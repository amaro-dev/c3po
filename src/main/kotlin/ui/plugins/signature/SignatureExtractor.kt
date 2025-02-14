package ui.plugins.signature

import commands.CommandRunner
import java.io.File

class SignatureExtractor {
    suspend fun getCertificateFingerprint(filePath: String): Result<AndroidPackageReport> {
        val output = CommandRunner.run(
            File(""),
            arrayOf(
                "/Users/roarodrigues/Library/Android/sdk/build-tools/34.0.0/apksigner",
                "verify",
                "--verbose",
                "--print-certs",
                filePath
            )
        ).onFailure {
            return Result.failure(it)
        }.getOrNull()!!
        val signerCount = output.substringAfter("Number of signers:")
            .takeIf { "Number of signers:" in it }
            ?.substringBefore("\n")
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it.isNotBlank() }
        println("Signers: '$output'")
        val signer = Signer.fromParams(
            *output.substringAfter("Signer #1 certificate DN: ")
                .substringBefore("\n")
                .split(",")
                .toTypedArray()
        )
        val sha256 = output.substringAfter("Signer #1 certificate SHA-256 digest: ").substringBefore("\n")
        val sha1 = output.substringAfter("Signer #1 certificate SHA-1 digest: ").substringBefore("\n")
        val md5 = output.substringAfter("Signer #1 certificate MD5 digest: ").substringBefore("\n")
        return Result.success(
            AndroidPackageReport(
                SignatureInfo(signer, sha256, sha1, md5),
                signerCount?.toInt() ?: 0
            )
        )
    }
}
