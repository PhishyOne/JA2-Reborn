package com.ja2.reborn

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

data class VerificationResult(
    val passed: Boolean,
    val reason: String? = null
)

object UpdateApkVerifier {

    private const val TAG = "UpdateApkVerifier"
    private const val EXPECTED_PACKAGE_NAME = "com.ja2.reborn"

    // -- SHA256 --------------------------------------------------------------

    fun computeSha256(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.w(TAG, "SHA256 computation failed", e)
            null
        }
    }

    // -- Full verification ---------------------------------------------------

    fun verifyApk(
        context: Context,
        apkFile: File,
        expectedSize: Long = -1L,
        expectedDigest: String? = null
    ): VerificationResult {
        if (!apkFile.exists() || !apkFile.canRead()) {
            return VerificationResult(false, "APK file not found or not readable")
        }

        if (expectedSize > 0 && apkFile.length() != expectedSize) {
            apkFile.delete()
            return VerificationResult(false, "APK size mismatch")
        }

        if (expectedDigest != null) {
            val actualDigest = computeSha256(apkFile)
            if (actualDigest == null) {
                return VerificationResult(false, "Could not compute SHA256")
            }
            val expected = expectedDigest.removePrefix("sha256:").removePrefix("SHA256:")
            if (!actualDigest.equals(expected, ignoreCase = true)) {
                apkFile.delete()
                return VerificationResult(false, "SHA256 digest mismatch")
            }
        }

        val apkInfo = getApkPackageInfo(context, apkFile.absolutePath)
        if (apkInfo == null) {
            apkFile.delete()
            return VerificationResult(false, "Could not parse APK")
        }

        if (apkInfo.packageName != EXPECTED_PACKAGE_NAME) {
            apkFile.delete()
            return VerificationResult(false,
                "Package name mismatch: expected $EXPECTED_PACKAGE_NAME, got ${apkInfo.packageName}")
        }

        val installedInfo = getInstalledPackageInfo(context)
        if (installedInfo == null) {
            return VerificationResult(false, "Could not read installed package info")
        }

        val installedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            installedInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            installedInfo.versionCode.toLong()
        }

        val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            apkInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            apkInfo.versionCode.toLong()
        }

        if (apkVersionCode <= installedVersionCode) {
            apkFile.delete()
            return VerificationResult(false,
                "APK version $apkVersionCode is not newer than installed $installedVersionCode")
        }

        if (!compareSignatures(apkInfo, installedInfo)) {
            apkFile.delete()
            return VerificationResult(false, "APK signature does not match installed app")
        }

        return VerificationResult(true)
    }

    // -- Installer Intent ----------------------------------------------------

    fun createInstallerIntent(context: Context, apkFile: File): Intent? {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create installer intent", e)
            null
        }
    }

    fun needsInstallPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !context.packageManager.canRequestPackageInstalls()
    }

    fun createInstallPermissionIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    // -- Pending APK state (for Settings round-trip) -------------------------

    private const val PREFS_NAME = "ja2_reborn_update_prefs"
    private const val KEY_PENDING_APK_PATH = "pending_update_apk_path"
    private const val KEY_PENDING_APK_VERSION = "pending_update_version"

    fun savePendingApk(context: Context, apkFile: File, version: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_APK_PATH, apkFile.absolutePath)
            .putString(KEY_PENDING_APK_VERSION, version)
            .apply()
    }

    fun getPendingApk(context: Context): Pair<File, String>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString(KEY_PENDING_APK_PATH, null) ?: return null
        val version = prefs.getString(KEY_PENDING_APK_VERSION, null) ?: return null
        return Pair(File(path), version)
    }

    fun clearPendingApk(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PENDING_APK_PATH)
            .remove(KEY_PENDING_APK_VERSION)
            .apply()
    }

    // -- Package info helpers ------------------------------------------------

    fun getApkPackageInfo(context: Context, apkPath: String): PackageInfo? {
        return try {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            context.packageManager.getPackageArchiveInfo(apkPath, flags)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read APK package info", e)
            null
        }
    }

    fun getInstalledPackageInfo(context: Context): PackageInfo? {
        return try {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            context.packageManager.getPackageInfo(EXPECTED_PACKAGE_NAME, flags)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Installed package not found — this should not happen at runtime")
            null
        }
    }

    fun compareSignatures(apkInfo: PackageInfo, installedInfo: PackageInfo): Boolean {
        val apkHashes = getCertificateHashes(apkInfo)
        val installedHashes = getCertificateHashes(installedInfo)
        if (apkHashes.isEmpty() || installedHashes.isEmpty()) return false
        return apkHashes == installedHashes
    }

    fun getCertificateHashes(info: PackageInfo): List<String> {
        val certs = mutableListOf<X509Certificate>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = info.signingInfo ?: return emptyList()
            val signers = signingInfo.apkContentsSigners
            try {
                val certFactory = CertificateFactory.getInstance("X.509")
                signers.forEach { sig ->
                    try {
                        val cert = certFactory.generateCertificate(
                            java.io.ByteArrayInputStream(sig.toByteArray())
                        )
                        if (cert is X509Certificate) certs.add(cert)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        } else {
            @Suppress("DEPRECATION")
            val signatures = info.signatures ?: return emptyList()
            try {
                val certFactory = CertificateFactory.getInstance("X.509")
                signatures.forEach { sig ->
                    try {
                        val cert = certFactory.generateCertificate(
                            java.io.ByteArrayInputStream(sig.toByteArray())
                        )
                        if (cert is X509Certificate) certs.add(cert)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            certs.map { cert ->
                digest.digest(cert.encoded).joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to compute certificate hashes", e)
            emptyList()
        }
    }
}
