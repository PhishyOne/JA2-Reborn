package com.ja2.reborn

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class UpdateCheckerTest {

    private val json = Json { ignoreUnknownKeys = true }

    // -- SemVer ---------------------------------------------------------------

    @Test
    fun `parseStableSemVer parses plain semver`() {
        val v = UpdateChecker.parseStableSemVer("1.0.5")
        assertNotNull(v)
        assertEquals(1, v!!.major)
        assertEquals(0, v.minor)
        assertEquals(5, v.patch)
    }

    @Test
    fun `parseStableSemVer parses v-prefixed semver`() {
        val v = UpdateChecker.parseStableSemVer("v1.0.6")
        assertNotNull(v)
        assertEquals(1, v!!.major)
        assertEquals(0, v.minor)
        assertEquals(6, v.patch)
    }

    @Test
    fun `parseStableSemVer returns null for garbage`() {
        assertNull(UpdateChecker.parseStableSemVer(""))
        assertNull(UpdateChecker.parseStableSemVer("abc"))
        assertNull(UpdateChecker.parseStableSemVer("1.0"))
        assertNull(UpdateChecker.parseStableSemVer("1.0.5.0"))
    }

    @Test
    fun `parseStableSemVer returns null for negative versions`() {
        assertNull(UpdateChecker.parseStableSemVer("-1.0.0"))
    }

    @Test
    fun `isNewerVersion remote greater returns true`() {
        assertTrue(UpdateChecker.isNewerVersion("v1.0.6", "1.0.5"))
        assertTrue(UpdateChecker.isNewerVersion("1.0.10", "1.0.9"))
        assertTrue(UpdateChecker.isNewerVersion("1.1.0", "1.0.9"))
        assertTrue(UpdateChecker.isNewerVersion("2.0.0", "1.9.9"))
    }

    @Test
    fun `isNewerVersion same or older returns false`() {
        assertFalse(UpdateChecker.isNewerVersion("1.0.5", "1.0.5"))
        assertFalse(UpdateChecker.isNewerVersion("v1.0.4", "1.0.5"))
        assertFalse(UpdateChecker.isNewerVersion("1.0.4", "1.0.5"))
    }

    @Test
    fun `isNewerVersion unparseable returns false`() {
        assertFalse(UpdateChecker.isNewerVersion("", "1.0.5"))
        assertFalse(UpdateChecker.isNewerVersion("v1.0.6", ""))
        assertFalse(UpdateChecker.isNewerVersion("latest", "1.0.5"))
    }

    @Test
    fun `semVer ordering works correctly`() {
        val v1 = UpdateChecker.parseStableSemVer("1.0.10")!!
        val v2 = UpdateChecker.parseStableSemVer("1.0.5")!!
        val v3 = UpdateChecker.parseStableSemVer("1.1.0")!!
        val v4 = UpdateChecker.parseStableSemVer("2.0.0")!!

        assertTrue(v1 > v2)
        assertTrue(v3 > v2)
        assertTrue(v4 > v3)
        assertEquals(0, v1.compareTo(UpdateChecker.parseStableSemVer("1.0.10")!!))
    }

    // -- JSON -----------------------------------------------------------------

    @Test
    fun `github release JSON decodes with extra unknown fields`() {
        val raw = """
        {
            "tag_name": "v1.0.6",
            "name": "Release v1.0.6",
            "body": "Bug fixes and improvements",
            "draft": false,
            "prerelease": false,
            "published_at": "2026-06-20T12:00:00Z",
            "extra_field_that_does_not_exist": 42,
            "assets": [
                {
                    "name": "JA2RebornRelease1.0.6.apk",
                    "browser_download_url": "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/JA2RebornRelease1.0.6.apk",
                    "size": 100805416,
                    "content_type": "application/vnd.android.package-archive",
                    "download_count": 15
                }
            ]
        }
        """.trimIndent()

        val release: GitHubRelease = json.decodeFromString(raw)
        assertEquals("v1.0.6", release.tagName)
        assertEquals("Release v1.0.6", release.name)
        assertFalse(release.draft)
        assertFalse(release.prerelease)
        assertEquals(1, release.assets.size)
        assertEquals("JA2RebornRelease1.0.6.apk", release.assets[0].name)
        assertEquals(100805416L, release.assets[0].size)
    }

    @Test
    fun `github release with empty assets decodes`() {
        val raw = """{"tag_name": "v1.0.5", "assets": []}"""
        val release: GitHubRelease = json.decodeFromString(raw)
        assertEquals("v1.0.5", release.tagName)
        assertTrue(release.assets.isEmpty())
    }

    @Test
    fun `github release with missing optional fields decodes`() {
        val raw = """{"tag_name": "v1.0.4"}"""
        val release: GitHubRelease = json.decodeFromString(raw)
        assertEquals("v1.0.4", release.tagName)
        assertNull(release.name)
        assertNull(release.body)
        assertFalse(release.draft)
    }

    // -- Asset selection ------------------------------------------------------

    @Test
    fun `selectApkAsset picks exact name match`() {
        val release = GitHubRelease(
            tagName = "v1.0.6",
            assets = listOf(
                GitHubAsset(
                    name = "JA2RebornRelease1.0.6.apk",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/JA2RebornRelease1.0.6.apk",
                    size = 100_000_000L
                ),
                GitHubAsset(
                    name = "source.zip",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/source.zip",
                    size = 50_000L
                )
            )
        )

        val selected = UpdateChecker.selectApkAsset(release)
        assertNotNull(selected)
        assertEquals("JA2RebornRelease1.0.6.apk", selected!!.name)
    }

    @Test
    fun `selectApkAsset returns null when no APK`() {
        val release = GitHubRelease(
            tagName = "v1.0.6",
            assets = listOf(
                GitHubAsset(
                    name = "source.tar.gz",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/source.tar.gz",
                    size = 50_000L
                )
            )
        )

        assertNull(UpdateChecker.selectApkAsset(release))
    }

    @Test
    fun `selectApkAsset rejects debug and unsigned apks`() {
        val release = GitHubRelease(
            tagName = "v1.0.6",
            assets = listOf(
                GitHubAsset(
                    name = "JA2RebornRelease1.0.6-debug.apk",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/app-debug.apk",
                    size = 100_000_000L
                )
            )
        )

        assertNull(UpdateChecker.selectApkAsset(release))
    }

    @Test
    fun `selectApkAsset rejects zero size apk`() {
        val release = GitHubRelease(
            tagName = "v1.0.6",
            assets = listOf(
                GitHubAsset(
                    name = "JA2RebornRelease1.0.6.apk",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/JA2RebornRelease1.0.6.apk",
                    size = 0L
                )
            )
        )

        assertNull(UpdateChecker.selectApkAsset(release))
    }

    @Test
    fun `selectApkAsset falls back to single apk without exact name`() {
        val release = GitHubRelease(
            tagName = "v1.0.6",
            assets = listOf(
                GitHubAsset(
                    name = "JA2RebornRelease1.0.6.apk",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/JA2RebornRelease1.0.6.apk",
                    size = 100_000_000L
                )
            )
        )

        val selected = UpdateChecker.selectApkAsset(release)
        assertNotNull(selected)
        assertEquals("JA2RebornRelease1.0.6.apk", selected!!.name)
    }

    @Test
    fun `selectApkAsset returns null for multiple apks without exact match`() {
        val release = GitHubRelease(
            tagName = "v1.0.6",
            assets = listOf(
                GitHubAsset(
                    name = "JA2RebornRelease1.0.6-arm64.apk",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/app-arm64.apk",
                    size = 100_000_000L
                ),
                GitHubAsset(
                    name = "JA2RebornRelease1.0.6-armeabi.apk",
                    browserDownloadUrl = "https://github.com/RealTommyGreen/JA2-Reborn/releases/download/v1.0.6/app-armeabi.apk",
                    size = 100_000_000L
                )
            )
        )

        assertNull(UpdateChecker.selectApkAsset(release))
    }

    // -- SemVer edge cases ----------------------------------------------------

    @Test
    fun `semVer toString formats correctly`() {
        val v = UpdateChecker.parseStableSemVer("1.2.3")
        assertEquals("1.2.3", v.toString())
    }
}
