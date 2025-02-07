package core

import commands.CommandRunner
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor

class AndroidPackageMiddleware() : AsyncMiddlewareBase<AppState>() {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        if (action is Actions.LoadFile) {
            println("Certificate: ${getCertificateFingerprint(action.filePath)}")
        }
    }
}

object Actions : IAction {
    data class LoadFile(val filePath: String) : Action

}

//class AndroidPackageAnalyser() {
//    fun analyse(filePath: String): AndroidPackageReport {
//
//    }
//}

data class AndroidPackageReport(
    val signatureHash: String,
    val isDebugSigned: Boolean,
)
// Get signature HASH command
// keytool -printcert -jarfile <APK-PATH> | grep "SHA1: " | cut -d " " -f 3 | xxd -r -p | openssl base64
// keytool -printcert -jarfile <APK-PATH> | grep "SHA256: " | cut -d " " -f 3 | xxd -r -p | openssl base64

suspend fun getCertificateFingerprint(filePath: String): SignatureInfo {
    val output = CommandRunner.run(
        arrayOf(
            "/Users/roarodrigues/Library/Android/sdk/build-tools/34.0.0/apksigner",
            "verify",
            "--print-certs",
            filePath
        )
    ).onFailure {
        println("[FAILED] $it")
    }.onSuccess {
        println("[SUCCESS] $it")
    }.getOrNull()!!
    val signer = Signer.fromParams(
        *output.substringAfter("Signer #1 certificate DN: ").substringBefore("\n").split(",").toTypedArray()
    )
    val sha256 = output.substringAfter("Signer #1 certificate SHA-256 digest: ").substringBefore("\n")
    val sha1 = output.substringAfter("Signer #1 certificate SHA-1 digest: ").substringBefore("\n")
    val md5 = output.substringAfter("Signer #1 certificate MD5 digest: ").substringBefore("\n")
    return SignatureInfo(
        signer,
        sha256,
        sha1,
        md5
    )
}

//Signer #1 certificate DN: CN=Mateo Pedemonte, OU=Point - SmartPos, O=Mercado Libre, L=CABA, ST=Buenos Aires, C=54
//Signer #1 certificate SHA-256 digest: d286111fb2f4c04ce17050b7d068e5954236d71bfc4452d11d4d4bdc064697c6
//Signer #1 certificate SHA-1 digest: b62a3b6096152d1bfb1d839d0a594350a8b2ee9d
//Signer #1 certificate MD5 digest: 107ce01259eec8aed717c5cce09ccab3


// Get signer name command
// keytool -printcert -jarfile <APK-PATH> | grep "CN=" | cut -d "," -f1 | cut -d ":" -f2 | cut -d "=" -f2 | head -n 1

// Get certificate name command
// keytool -printcert -jarfile <APK-PATH> | grep "OU=" | cut -d "," -f2 | cut -d ":" -f2 | cut -d "=" -f2 | head -n 1


// Apksigner
//Number of signers: 1
//Verified using v1 scheme (JAR signing): true
//Verified using v2 scheme (APK Signature Scheme v2): true
//Verified using v3 scheme (APK Signature Scheme v3): false
//Verified using v3.1 scheme (APK Signature Scheme v3.1): false
//Verified using v4 scheme (APK Signature Scheme v4): false

data class Signer(
    val commonName: String,
    val organizationUnit: String,
    val organization: String,
    val locality: String,
    val state: String,
    val countryCode: String
) {
    companion object {
        fun fromParams(vararg params: String): Signer {
            if (params.size < 6) throw IncompleteSignerInformationException()
            val fields = params.map { it.trim().split("=") }.associate { it[0] to it[1] }
            if (!fields.keys.containsAll(
                    listOf(
                        "CN",
                        "OU",
                        "O",
                        "L",
                        "ST",
                        "C"
                    )
                )
            ) throw IncompleteSignerInformationException()
            return Signer(fields["CN"]!!, fields["OU"]!!, fields["O"]!!, fields["L"]!!, fields["ST"]!!, fields["C"]!!)
        }
    }
}

data class SignatureInfo(
    val signer: Signer,
    val sha256Digest: String,
    val shar1Digest: String,
    val md5Digest: String
)

class IncompleteSignerInformationException() : Exception()
