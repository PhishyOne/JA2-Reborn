package com.ja2.reborn

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String = "",
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList()
)

@Serializable
data class GitHubAsset(
    val name: String = "",
    @SerialName("browser_download_url") val browserDownloadUrl: String = "",
    val size: Long = 0L,
    @SerialName("content_type") val contentType: String? = null
)

data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<SemVer> {

    override fun compareTo(other: SemVer): Int {
        major.compareTo(other.major).let { if (it != 0) return it }
        minor.compareTo(other.minor).let { if (it != 0) return it }
        return patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"
}

object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val GITHUB_API_URL = "https://api.github.com/repos/RealTommyGreen/JA2-Reborn/releases/latest"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 30_000

    private val json = Json { ignoreUnknownKeys = true }

    private val apkNameRegex = Regex("^JA2RebornRelease\\d+\\.\\d+\\.\\d+\\.apk$")

    private fun safeLog(level: Int, tag: String, msg: String, tr: Throwable? = null) {
        try {
            Log.println(level, tag, if (tr != null) "$msg\n${Log.getStackTraceString(tr)}" else msg)
        } catch (_: RuntimeException) { }
    }

    private fun logW(msg: String, tr: Throwable? = null) = safeLog(Log.WARN, TAG, msg, tr)
    private fun logI(msg: String) = safeLog(Log.INFO, TAG, msg)
    private fun logE(msg: String) = safeLog(Log.ERROR, TAG, msg)

    // -- JSON / API ----------------------------------------------------------

    fun fetchLatestRelease(): GitHubRelease? {
        return try {
            val connection = URL(GITHUB_API_URL).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2026-03-10")
                setRequestProperty("User-Agent", "JA2-Reborn-Android")
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logW("GitHub API returned HTTP $responseCode")
                connection.disconnect()
                return null
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            json.decodeFromString<GitHubRelease>(body)
        } catch (e: IOException) {
            logW("Failed to fetch latest release", e)
            null
        } catch (e: Exception) {
            logW("Unexpected error fetching latest release", e)
            null
        }
    }

    // -- SemVer ---------------------------------------------------------------

    fun parseStableSemVer(value: String): SemVer? {
        val trimmed = value.trim()
        val withoutPrefix = if (trimmed.startsWith('v')) trimmed.substring(1) else trimmed
        val parts = withoutPrefix.split(".")
        if (parts.size != 3) return null
        val major = parts[0].toIntOrNull() ?: return null
        val minor = parts[1].toIntOrNull() ?: return null
        val patch = parts[2].toIntOrNull() ?: return null
        if (major < 0 || minor < 0 || patch < 0) return null
        return SemVer(major, minor, patch)
    }

    fun isNewerVersion(remoteTag: String, localVersionName: String): Boolean {
        val remote = parseStableSemVer(remoteTag) ?: return false
        val local = parseStableSemVer(localVersionName) ?: return false
        return remote > local
    }

    // -- Asset selection ------------------------------------------------------

    fun selectApkAsset(release: GitHubRelease): GitHubAsset? {
        val candidates = release.assets.filter { asset ->
            asset.name.endsWith(".apk", ignoreCase = true)
                && !asset.name.contains("debug", ignoreCase = true)
                && !asset.name.contains("unsigned", ignoreCase = true)
                && asset.browserDownloadUrl.startsWith("https://")
                && asset.size > 0L
        }

        if (candidates.isEmpty()) {
            logW("No valid APK assets found in release ${release.tagName}")
            return null
        }

        val exact = candidates.firstOrNull { it.name.matches(apkNameRegex) }
        if (exact != null) return exact

        if (candidates.size == 1) {
            logI("Falling back to single APK asset: ${candidates[0].name}")
            return candidates[0]
        }

        logW("Ambiguous APK assets: ${candidates.size} candidates, no exact name match")
        return null
    }

    // -- Download -------------------------------------------------------------

    data class DownloadProgress(val bytesDownloaded: Long, val totalBytes: Long)

    fun downloadApk(
        asset: GitHubAsset,
        targetVersion: String,
        cacheDir: File,
        onProgress: ((DownloadProgress) -> Unit)? = null
    ): File? {
        return try {
            val updateDir = File(cacheDir, "apk_update")
            if (!updateDir.exists() && !updateDir.mkdirs()) {
                logE("Failed to create apk_update directory")
                return null
            }

            val finalFile = File(updateDir, "JA2RebornRelease$targetVersion.apk")
            val partFile = File(updateDir, "JA2RebornRelease$targetVersion.apk.part")
            partFile.delete()

            val connection = URL(asset.browserDownloadUrl).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logW("Download returned HTTP $responseCode")
                connection.disconnect()
                return null
            }

            val contentLength = connection.contentLengthLong
            val expectedSize = if (contentLength > 0) contentLength else asset.size

            connection.inputStream.use { input ->
                FileOutputStream(partFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Long = 0
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read
                        onProgress?.invoke(DownloadProgress(bytesRead, expectedSize))
                    }
                }
            }
            connection.disconnect()

            val actualSize = partFile.length()
            if (expectedSize > 0 && actualSize != expectedSize) {
                logW("Download size mismatch: expected $expectedSize, got $actualSize")
                partFile.delete()
                return null
            }

            finalFile.delete()
            if (!partFile.renameTo(finalFile)) {
                logE("Failed to rename .part to final APK")
                partFile.delete()
                return null
            }

            logI("Downloaded ${finalFile.length()} bytes to ${finalFile.name}")
            finalFile
        } catch (e: IOException) {
            logW("Download failed", e)
            null
        } catch (e: Exception) {
            logW("Unexpected error during download", e)
            null
        }
    }
}
