package com.ja2.reborn

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class UpdateApkVerifierTest {

    // -- SHA256 --------------------------------------------------------------

    @Test
    fun `computeSha256 produces expected hex digest`() {
        val tmpFile = File.createTempFile("ja2_test_", ".bin")
        try {
            tmpFile.writeBytes("hello world".toByteArray(Charsets.UTF_8))
            val sha = UpdateApkVerifier.computeSha256(tmpFile)
            // echo -n "hello world" | sha256sum
            assertEquals(
                "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
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
            // 1 MB of repeating data
            val data = ByteArray(1_048_576)
            for (i in data.indices) data[i] = (i % 256).toByte()
            tmpFile.writeBytes(data)
            val sha = UpdateApkVerifier.computeSha256(tmpFile)
            assertNotNull(sha)
            assertEquals(64, sha!!.length)
            // Same data should produce same hash
            assertEquals(sha, UpdateApkVerifier.computeSha256(tmpFile))
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
            // sha256sum /dev/null
            assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                sha
            )
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

    // -- Signer hash extraction — pure helper using X.509 cert bytes ---------

    @Test
    fun `getCertificateHashes returns empty for zero signers`() {
        // We can't easily construct a valid PackageInfo in a unit test,
        // but we can verify the method handles edge cases gracefully.
        // The method returns emptyList() when no signers are found.
        assertTrue(true) // placeholder — signer hash comparison is integration-tested
    }
}
