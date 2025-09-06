package core.facade.update

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.nio.file.Files
import java.security.Security
import java.util.Base64

/**
 * Verifies minisign signatures using BouncyCastle Ed25519 implementation.
 *
 * Minisign format:
 * - Public key: base64(algorithm(2) + Ed25519_key(32) + key_id(8)) = 42 bytes
 * - Signature file (.minisig): base64(algorithm(2) + signature(64) + key_id(8)) = 74 bytes
 */
class MinisignVerifier {

    companion object {
        init {
            // Register BouncyCastle provider if not already registered
            if (Security.getProvider("BC") == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }

    fun verifyFile(
        filePath: File,
        signatureFilePath: File,
        publicKeyString: String
    ): Boolean {
        return try {
            println("[MinisignVerifier] Starting BouncyCastle minisign verification...")
            
            // Parse the public key
            val publicKeyBytes = parseMinisignPublicKey(publicKeyString)

            // Parse the signature file
            val signatureData = parseMinisignSignature(signatureFilePath.readText())

            // Read the file content
            val fileContent = Files.readAllBytes(filePath.toPath())

            // Verify the signature using BouncyCastle
            verifySignature(fileContent, signatureData, publicKeyBytes)
        } catch (e: Exception) {
            println("[MinisignVerifier] BouncyCastle verification failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun parseMinisignPublicKey(publicKeyString: String): ByteArray {
        println("[MinisignVerifier] Parsing public key: $publicKeyString")

        // The entire string IS base64 - "RW" is part of the encoding, not a prefix
        val decoded = try {
            Base64.getDecoder().decode(publicKeyString)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid minisign public key: not valid Base64", e)
        }

        println("[MinisignVerifier] Decoded key size: ${decoded.size} bytes")
        println("[MinisignVerifier] Decoded key hex: ${decoded.joinToString("") { "%02x".format(it) }}")

        // Validate size: should be 42 bytes (algorithm(2) + key_id(8) + Ed25519_key(32))
        if (decoded.size != 42) {
            throw IllegalArgumentException("Invalid minisign public key size: ${decoded.size} (expected 42)")
        }

        // Validate algorithm: should be 'E' 'd' (0x45 0x64)
        val alg0 = decoded[0].toInt() and 0xFF
        val alg1 = decoded[1].toInt() and 0xFF
        if (alg0 != 0x45 || alg1 != 0x64) { // "Ed"
            val hex = "%02x%02x".format(alg0, alg1)
            throw IllegalArgumentException("Unsupported minisign public key algorithm: 0x$hex (expected 'Ed')")
        }

        println("[MinisignVerifier] Algorithm: ${String(byteArrayOf(decoded[0], decoded[1]))}")

        // Extract Ed25519 public key: algorithm(2) + key_id(8) + Ed25519_key(32)
        // Layout: [0..1]=algorithm, [2..9]=key_id, [10..41]=Ed25519 public key (32 bytes)
        val publicKey = decoded.copyOfRange(10, 42)
        println("[MinisignVerifier] Extracted public key (32 bytes): ${publicKey.joinToString("") { "%02x".format(it) }}")

        return publicKey
    }

    private fun parseMinisignSignature(sigText: String): MinisignSig {
        println("[MinisignVerifier] Parsing signature text:")
        sigText.lineSequence().forEachIndexed { index, line ->
            println("[MinisignVerifier] Line $index: $line")
        }

        // Skip "untrusted comment:" and get the base64 signature line
        val base64Line = sigText.lineSequence()
            .map { it.trim() }
            .firstOrNull {
                it.isNotEmpty() && !it.lowercase().startsWith("untrusted comment:") && !it.lowercase()
                    .startsWith("trusted comment:")
            }
            ?: throw IllegalArgumentException("Empty minisign signature")

        println("[MinisignVerifier] Selected base64 signature line: $base64Line")

        val decoded = Base64.getDecoder().decode(base64Line)
        println("[MinisignVerifier] Decoded signature bytes: ${decoded.size}")
        println("[MinisignVerifier] Signature hex: ${decoded.joinToString("") { "%02x".format(it) }}")

        return when (decoded.size) {
            74 -> {
                // Standard format: algorithm(2) + key_id(8) + signature(64)
                val alg = String(byteArrayOf(decoded[0], decoded[1])) // "Ed" or "B+"
                val keyId = decoded.copyOfRange(2, 10)  // 8 bytes key ID
                val sig = decoded.copyOfRange(10, 74)   // 64 bytes signature
                println(
                    "[MinisignVerifier] Parsed: algorithm='$alg', keyId=${keyId.joinToString("") { "%02x".format(it) }}, sig=${
                        sig.joinToString(
                            ""
                        ) { "%02x".format(it) }
                    }"
                )
                MinisignSig(sig, keyId, alg)
            }

            72 -> {
                // Legacy format without algorithm bytes: signature(64) + key_id(8)
                val sig = decoded.copyOfRange(0, 64)
                val keyId = decoded.copyOfRange(64, 72)
                MinisignSig(sig, keyId, "Ed")
            }

            else -> throw IllegalArgumentException("Invalid minisign signature size: ${decoded.size} (expected 74 or 72)")
        }
    }

    private fun blake2b512(data: ByteArray): ByteArray {
        val d = Blake2bDigest(512) // 64 bytes
        d.update(data, 0, data.size)
        val out = ByteArray(64)
        d.doFinal(out, 0)
        return out
    }

    private fun verifySignature(
        fileContent: ByteArray,
        minisignSig: MinisignSig,
        publicKeyBytes: ByteArray
    ): Boolean {
        return try {
            println("[MinisignVerifier] Starting signature verification...")
            println("[MinisignVerifier] Algorithm: ${minisignSig.alg}")
            println("[MinisignVerifier] File size: ${fileContent.size} bytes")
            println("[MinisignVerifier] Signature size: ${minisignSig.signature64.size} bytes")
            println("[MinisignVerifier] Public key size: ${publicKeyBytes.size} bytes")

            // For large files (>1MB), minisign always uses Blake2b pre-hashing even with "Ed" algorithm
            // This is indicated by the "hashed" comment in the trusted comment line
            val shouldUseBlake2b = fileContent.size > 1048576 // 1MB threshold

            val message = when (minisignSig.alg.uppercase()) {
                "ED" -> {
                    if (shouldUseBlake2b) {
                        println("[MinisignVerifier] Using Blake2b-512 pre-hash for large file (${fileContent.size} bytes) with Ed algorithm")
                        val hashed = blake2b512(fileContent)
                        println("[MinisignVerifier] Blake2b hash computed: ${hashed.size} bytes")
                        println("[MinisignVerifier] Blake2b hash hex: ${hashed.joinToString("") { "%02x".format(it) }}")
                        hashed
                    } else {
                        println("[MinisignVerifier] Using raw file content for small Ed algorithm file")
                        fileContent
                    }
                }

                "B+" -> {
                    // For "B+" algorithm: always use Blake2b-512 pre-hash
                    println("[MinisignVerifier] Using Blake2b-512 pre-hash for B+ algorithm")
                    val hashed = blake2b512(fileContent)
                    println("[MinisignVerifier] Blake2b hash computed: ${hashed.size} bytes")
                    hashed
                }

                else -> {
                    throw IllegalArgumentException("Unsupported minisign algorithm: ${minisignSig.alg}")
                }
            }

            println("[MinisignVerifier] Message to verify: ${message.size} bytes")
            println("[MinisignVerifier] Message hex: ${message.take(32).joinToString("") { "%02x".format(it) }}...")

            // Create Ed25519 public key and signer
            val publicKeyParams = Ed25519PublicKeyParameters(publicKeyBytes, 0)
            val signer = Ed25519Signer()
            signer.init(false, publicKeyParams) // false = verify mode

            // Update signer with the message
            signer.update(message, 0, message.size)

            // Verify the signature
            val result = signer.verifySignature(minisignSig.signature64)
            println("[MinisignVerifier] Verification result: $result")

            result
        } catch (e: Exception) {
            println("[MinisignVerifier] Signature verification error: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    data class MinisignSig(
        val signature64: ByteArray, // 64 bytes
        val keyId8: ByteArray,      // 8 bytes
        val alg: String             // "Ed" (raw) ou "B+" / "ED" (pre-hash Blake2b-512)
    )

}