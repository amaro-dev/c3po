package core.facade.update

import org.junit.jupiter.api.Test
import java.io.File

class MinisignVerifierDebugTest {

    @Test
    fun debugMinisignVerification() {
        val verifier = MinisignVerifier()
        val publicKey = "RWQiuKyxbd0jiAVaNZy1186PTYOFgeU5hGi4BIWaEaI8Shyek3kYHsDr"

        val realZipFile = File("/tmp/minisign-debug/c3po-3.2.11-macos.zip")
        val realSigFile = File("/tmp/minisign-debug/c3po-3.2.11-macos.zip.minisig")

        // Skip test if files not available
        if (!realZipFile.exists() || !realSigFile.exists()) {
            println("Skipping minisign test - external files not available")
            return
        }

        val result = verifier.verifyFile(realZipFile, realSigFile, publicKey)

        if (result) {
            println("✅ MinisignVerifier successfully verified real signature")
        } else {
            throw AssertionError("MinisignVerifier failed to verify valid signature")
        }
    }
}