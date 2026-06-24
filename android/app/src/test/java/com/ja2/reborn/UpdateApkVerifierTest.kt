package com.ja2.reborn

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class UpdateApkVerifierTest {

    // -- Pure helpers: normalizeDigest ---------------------------------------

    @Test
    fun `normalizeDigest strips sha256 prefix`() {
        assertEquals("abc123", UpdateApkVerifier.normalizeDigest("sha256:abc123"))
    }

    @Test
    fun `normalizeDigest strips SHA256 prefix case insensitive`() {
        assertEquals("abc123", UpdateApkVerifier.normalizeDigest("SHA256:abc123"))
    }

    @Test
    fun `normalizeDigest passes through plain digest`() {
        assertEquals("abc123", UpdateApkVerifier.normalizeDigest("abc123"))
    }

    // -- Pure helpers: isVersionCodeNewer ------------------------------------

    @Test
    fun `isVersionCodeNewer true when apk code is greater`() {
        assertTrue(UpdateApkVerifier.isVersionCodeNewer(1000006L, 1000005L))
    }

    @Test
    fun `isVersionCodeNewer false when equal`() {
        assertFalse(UpdateApkVerifier.isVersionCodeNewer(1000005L, 1000005L))
    }

    @Test
    fun `isVersionCodeNewer false when installed is greater`() {
        assertFalse(UpdateApkVerifier.isVersionCodeNewer(1000004L, 1000005L))
    }

    // -- Pure helpers: signaturesMatch ---------------------------------------

    @Test
    fun `signaturesMatch true for identical hash lists`() {
        val hashes = listOf("aaa", "bbb")
        assertTrue(UpdateApkVerifier.signaturesMatch(hashes, hashes))
    }

    @Test
    fun `signaturesMatch false for different hash lists`() {
        assertFalse(UpdateApkVerifier.signaturesMatch(
            listOf("aaa", "bbb"), listOf("aaa", "ccc")))
    }

    @Test
    fun `signaturesMatch false for different length lists`() {
        assertFalse(UpdateApkVerifier.signaturesMatch(
            listOf("aaa"), listOf("aaa", "bbb")))
    }

    @Test
    fun `signaturesMatch false when either list is empty`() {
        assertFalse(UpdateApkVerifier.signaturesMatch(emptyList(), listOf("aaa")))
        assertFalse(UpdateApkVerifier.signaturesMatch(listOf("aaa"), emptyList()))
        assertFalse(UpdateApkVerifier.signaturesMatch(emptyList(), emptyList()))
    }

    @Test
    fun `signaturesMatch true for single signer match`() {
        assertTrue(UpdateApkVerifier.signaturesMatch(
            listOf("833a992d08cdf66f9abcbb239e16419a80c75aba216636e339fa5aff6b67c44d"),
            listOf("833a992d08cdf66f9abcbb239e16419a80c75aba216636e339fa5aff6b67c44d")))
    }

    // -- SHA256 --------------------------------------------------------------

    @Test
    fun `computeSha256 produces expected hex digest`() {
        val tmpFile = File.createTempFile("ja2_test_", ".bin")
        try {
            tmpFile.writeBytes("hello world".toByteArray(Charsets.UTF_8))
            val sha = UpdateApkVerifier.computeSha256(tmpFile)
            assertEquals(
                "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
                sha
            )
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun `computeSha256 empty file produces known digest`() {
        val tmpFile = File.createTempFile("ja2_test_empty_", ".bin")
        try {
            tmpFile.writeBytes(ByteArray(0))
            val sha = UpdateApkVerifier.computeSha256(tmpFile)
            assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                sha
            )
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun `computeSha256 returns consistent results for larger data`() {
        val tmpFile = File.createTempFile("ja2_test_large_", ".bin")
        try {
            val data = ByteArray(1_048_576)
            for (i in data.indices) data[i] = (i % 256).toByte()
            tmpFile.writeBytes(data)
            val sha = UpdateApkVerifier.computeSha256(tmpFile)
            assertNotNull(sha)
            assertEquals(64, sha!!.length)
            assertEquals(sha, UpdateApkVerifier.computeSha256(tmpFile))
        } finally {
            tmpFile.delete()
        }
    }

    // -- VerificationResult --------------------------------------------------

    @Test
    fun `VerificationResult passed true has null reason`() {
        val r = VerificationResult(true)
        assertTrue(r.passed)
        assertNull(r.reason)
    }

    @Test
    fun `VerificationResult failed has reason`() {
        val r = VerificationResult(false, "APK size mismatch")
        assertFalse(r.passed)
        assertEquals("APK size mismatch", r.reason)
    }
}
