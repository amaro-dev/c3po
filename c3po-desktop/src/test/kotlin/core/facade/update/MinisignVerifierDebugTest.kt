package core.facade.update

import org.junit.jupiter.api.Test
import java.io.File

class MinisignVerifierDebugTest {

    @Test
    fun debugMinisignVerification() {
        val verifier = MinisignVerifier()
        val publicKey = "RWQiuKyxbd0jiAVaNZy1186PTYOFgeU5hGi4BIWaEaI8Shyek3kYHsDr"

        println("Testing pure BouncyCastle minisign verification with REAL GitHub release files...")
        println("Public key: $publicKey")

        // Use real files from GitHub release
        val realZipFile = File("/tmp/minisign-debug/c3po-3.2.11-macos.zip")
        val realSigFile = File("/tmp/minisign-debug/c3po-3.2.11-macos.zip.minisig")

        println("Real ZIP file: ${realZipFile.absolutePath}")
        println("Real sig file: ${realSigFile.absolutePath}")

        if (!realZipFile.exists() || !realSigFile.exists()) {
            println("ERROR: Real test files not found. Make sure /tmp/minisign-debug/ contains the downloaded files")
            return
        }

        try {
            println("Testing BouncyCastle minisign verification with REAL signature...")
            val result = verifier.verifyFile(realZipFile, realSigFile, publicKey)
            println("BouncyCastle verification result: $result")

            if (result) {
                println("SUCCESS: BouncyCastle implementation verified the real signature!")
            } else {
                println("FAILED: BouncyCastle implementation rejected the real signature")
            }
        } catch (e: Exception) {
            println("Exception during verification: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }
    }
}