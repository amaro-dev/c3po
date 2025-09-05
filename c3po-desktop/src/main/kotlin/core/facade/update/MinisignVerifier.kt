package core.facade.update

import java.io.File
import java.nio.file.Files
import java.security.KeyFactory
import java.security.Signature
import java.util.Base64

/**
 * Verifies minisign signatures using Java 17 native Ed25519 support.
 *
 * Minisign format:
 * - Public key: base64-encoded Ed25519 public key with algorithm prefix
 * - Signature file (.minisig): base64-encoded signature with metadata
 *
 * This implementation uses Java's built-in EdDSA for verification.
 */
class MinisignVerifier {

    companion object {
        private const val MINISIGN_ALGORITHM_ID = "Ed"
        private const val SIGNATURE_ALGORITHM = "Ed25519"
        private const val PUBLIC_KEY_SIZE = 32
        private const val SIGNATURE_SIZE = 64
    }

    /**
     * Verifies a file against its minisign signature.
     *
     * @param filePath Path to the file to verify
     * @param signatureFilePath Path to the .minisig signature file
     * @param publicKeyBase64 Base64-encoded minisign public key
     * @return true if signature is valid, false otherwise
     * @throws Exception if verification process fails
     */
    fun verifyFile(
        filePath: File,
        signatureFilePath: File,
        publicKeyBase64: String
    ): Boolean {
        return try {
            // Parse the public key
            val publicKey = parseMinisignPublicKey(publicKeyBase64)

            // Parse the signature file
            val signatureData = parseMinisignSignature(signatureFilePath)

            // Read the file content
            val fileContent = Files.readAllBytes(filePath.toPath())

            // Verify the signature
            verifySignature(fileContent, signatureData, publicKey)
        } catch (e: Exception) {
            println("[MinisignVerifier] Verification failed: ${e.message}")
            false
        }
    }

    private fun parseMinisignPublicKey(publicKeyBase64: String): java.security.PublicKey {
        val decoded = Base64.getDecoder().decode(publicKeyBase64.trim())

        if (decoded.size != 34) {
            throw IllegalArgumentException("Invalid minisign public key size: ${decoded.size} (expected 34)")
        }

        // Check algorithm identifier (first 2 bytes should be "Ed")
        val algorithmId = String(decoded, 0, 2, Charsets.UTF_8)
        if (algorithmId != MINISIGN_ALGORITHM_ID) {
            throw IllegalArgumentException("Unsupported algorithm: $algorithmId (expected $MINISIGN_ALGORITHM_ID)")
        }

        // Extract the 32-byte Ed25519 public key
        val publicKeyBytes = decoded.copyOfRange(2, 34)

        // Use X509EncodedKeySpec for raw Ed25519 key - Java 17 supports this format
        // For Ed25519, we need to create the proper ASN.1 structure
        val x509PublicKey = createEd25519X509Key(publicKeyBytes)
        val keySpec = java.security.spec.X509EncodedKeySpec(x509PublicKey)
        val keyFactory = KeyFactory.getInstance(SIGNATURE_ALGORITHM)
        return keyFactory.generatePublic(keySpec)
    }

    private fun createEd25519X509Key(rawKey: ByteArray): ByteArray {
        // Ed25519 public key in X.509 format
        // ASN.1 structure: SEQUENCE { SEQUENCE { OID }, BIT STRING }
        // OID for Ed25519: 1.3.101.112
        val ed25519Oid = byteArrayOf(
            0x30, 0x2a,                           // SEQUENCE (42 bytes)
            0x30, 0x05,                           // SEQUENCE (5 bytes) - AlgorithmIdentifier
            0x06, 0x03, 0x2b, 0x65, 0x70,         // OID 1.3.101.112 (Ed25519)
            0x03, 0x21, 0x00                      // BIT STRING (33 bytes: 1 unused bits byte + 32 key bytes)
        )
        return ed25519Oid + rawKey
    }

    /**
     * Parses a minisign signature file.
     * Format: trusted_comment + signature_line
     * Signature line: algorithm || key_id || signature (2 + 8 + 64 = 74 bytes)
     */
    private fun parseMinisignSignature(signatureFile: File): ByteArray {
        val lines = signatureFile.readLines().filter { it.isNotBlank() }

        if (lines.size < 2) {
            throw IllegalArgumentException("Invalid minisign signature file format")
        }

        // The signature is on the second line (first line is trusted comment)
        val signatureLine = lines[1].trim()
        val decoded = Base64.getDecoder().decode(signatureLine)

        if (decoded.size != 74) {
            throw IllegalArgumentException("Invalid signature size: ${decoded.size} (expected 74)")
        }

        // Check algorithm identifier
        val algorithmId = String(decoded, 0, 2, Charsets.UTF_8)
        if (algorithmId != MINISIGN_ALGORITHM_ID) {
            throw IllegalArgumentException("Unsupported signature algorithm: $algorithmId")
        }

        // Extract the 64-byte signature (skip algorithm id + key id)
        return decoded.copyOfRange(10, 74)
    }

    /**
     * Verifies the signature using Java's native Ed25519 implementation.
     */
    private fun verifySignature(
        fileContent: ByteArray,
        signatureBytes: ByteArray,
        publicKey: java.security.PublicKey
    ): Boolean {
        val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
        signature.initVerify(publicKey)
        signature.update(fileContent)
        return signature.verify(signatureBytes)
    }

    /**
     * Utility method to validate a minisign public key format.
     */
    fun isValidPublicKey(publicKeyBase64: String): Boolean {
        return try {
            parseMinisignPublicKey(publicKeyBase64)
            true
        } catch (e: Exception) {
            false
        }
    }
}