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
            val publicKeyBytes = parseMinisignPublicKey(publicKeyString)
            val signatureData = parseMinisignSignature(signatureFilePath.readText())
            val fileContent = Files.readAllBytes(filePath.toPath())

            verifySignature(fileContent, signatureData, publicKeyBytes)
        } catch (e: Exception) {
            false
        }
    }

    private fun parseMinisignPublicKey(publicKeyString: String): ByteArray {
        val decoded = try {
            Base64.getDecoder().decode(publicKeyString)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid minisign public key: not valid Base64", e)
        }

        if (decoded.size != 42) {
            throw IllegalArgumentException("Invalid minisign public key size: ${decoded.size} (expected 42)")
        }

        val alg0 = decoded[0].toInt() and 0xFF
        val alg1 = decoded[1].toInt() and 0xFF
        if (alg0 != 0x45 || alg1 != 0x64) {
            val hex = "%02x%02x".format(alg0, alg1)
            throw IllegalArgumentException("Unsupported minisign public key algorithm: 0x$hex (expected 'Ed')")
        }

        return decoded.copyOfRange(10, 42)
    }

    private fun parseMinisignSignature(sigText: String): MinisignSig {
        val base64Line = sigText.lineSequence()
            .map { it.trim() }
            .firstOrNull {
                it.isNotEmpty() &&
                        !it.lowercase().startsWith("untrusted comment:") &&
                        !it.lowercase().startsWith("trusted comment:")
            }
            ?: throw IllegalArgumentException("Empty minisign signature")

        val decoded = Base64.getDecoder().decode(base64Line)

        return when (decoded.size) {
            74 -> {
                val alg = String(byteArrayOf(decoded[0], decoded[1]))
                val keyId = decoded.copyOfRange(2, 10)
                val sig = decoded.copyOfRange(10, 74)
                MinisignSig(sig, keyId, alg)
            }
            72 -> {
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
            val shouldUseBlake2b = fileContent.size > 1048576 // 1MB threshold

            val message = when (minisignSig.alg.uppercase()) {
                "ED" -> {
                    if (shouldUseBlake2b) {
                        blake2b512(fileContent)
                    } else {
                        fileContent
                    }
                }
                "B+" -> {
                    blake2b512(fileContent)
                }
                else -> {
                    throw IllegalArgumentException("Unsupported minisign algorithm: ${minisignSig.alg}")
                }
            }

            val publicKeyParams = Ed25519PublicKeyParameters(publicKeyBytes, 0)
            val signer = Ed25519Signer()
            signer.init(false, publicKeyParams)
            signer.update(message, 0, message.size)

            signer.verifySignature(minisignSig.signature64)
        } catch (e: Exception) {
            false
        }
    }

    data class MinisignSig(
        val signature64: ByteArray, // 64 bytes
        val keyId8: ByteArray,      // 8 bytes
        val alg: String             // "Ed" (raw) ou "B+" / "ED" (pre-hash Blake2b-512)
    )

}