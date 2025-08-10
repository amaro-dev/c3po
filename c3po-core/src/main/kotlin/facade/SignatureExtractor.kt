package facade

import commands.CommandRunner
import models.AndroidPackageReport
import models.SignatureInfo
import models.Signer
import java.io.File

class SignatureExtractor {
    companion object {
        private const val V1_DISCLAIMER = "Verified using v1 scheme (JAR signing): "
        private const val V2_DISCLAIMER = "Verified using v2 scheme (APK Signature Scheme v2): "
        private const val V3_DISCLAIMER = "Verified using v3 scheme (APK Signature Scheme v3): "
        private const val V3_1_DISCLAIMER = "Verified using v3.1 scheme (APK Signature Scheme v3.1): "
        private const val V4_DISCLAIMER = "Verified using v4 scheme (APK Signature Scheme v4): "
    }

    suspend fun getCertificateFingerprint(filePath: String): Result<AndroidPackageReport> {
        val output =
            CommandRunner
                .run(
                    File(""),
                    arrayOf(
                        "/Users/roarodrigues/Library/Android/sdk/build-tools/34.0.0/apksigner",
                        "verify",
                        "--verbose",
                        "--print-certs",
                        filePath,
                    ),
                ).onFailure {
                    return Result.failure(it)
                }.getOrNull()!!
        val signerCount =
            output
                .takeIf { "Number of signers:" in it }
                ?.substringAfter("Number of signers:")
                ?.substringBefore("\n")
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it.isNotBlank() }

        val signer =
            Signer.fromParams(
                *output
                    .substringAfter("Signer #1 certificate DN: ")
                    .substringBefore("\n")
                    .split(",")
                    .toTypedArray(),
            )
        val sha256 = output.substringAfter("Signer #1 certificate SHA-256 digest: ").substringBefore("\n")
        val sha1 = output.substringAfter("Signer #1 certificate SHA-1 digest: ").substringBefore("\n")
        val md5 = output.substringAfter("Signer #1 certificate MD5 digest: ").substringBefore("\n")
        return Result.success(
            AndroidPackageReport(
                filePath,
                SignatureInfo(signer, sha256, sha1, md5),
                signerCount?.toInt() ?: 0,
                isCompliant(output, V1_DISCLAIMER),
                isCompliant(output, V2_DISCLAIMER),
                isCompliant(output, V3_DISCLAIMER),
                isCompliant(output, V3_1_DISCLAIMER),
                isCompliant(output, V4_DISCLAIMER),
            ),
        )
    }

    private fun isCompliant(
        output: String,
        message: String,
    ): BoolState =
        output
            .takeIf { message in output }
            ?.substringAfter(message)
            ?.substringBefore("\n")
            ?.let { BoolState.valueOf(it.uppercase().trim()) } ?: BoolState.NOT_FOUND
}

enum class BoolState {
    TRUE,
    FALSE,
    NOT_FOUND,
}
