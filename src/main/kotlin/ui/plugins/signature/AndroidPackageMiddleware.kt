package ui.plugins.signature

import core.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor

class AndroidPackageMiddleware(
    private val signatureExtractor: SignatureExtractor
) : AsyncMiddlewareBase<AppState>() {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        if (action is Actions.LoadFile) {
            println("Certificate: ${signatureExtractor.getCertificateFingerprint(action.filePath)}")
        }
    }
}

sealed interface Actions : IAction {
    data class LoadFile(val filePath: String) : Actions
}


//Signer #1 certificate DN: CN=Mateo Pedemonte, OU=Point - SmartPos, O=Mercado Libre, L=CABA, ST=Buenos Aires, C=54
//Signer #1 certificate SHA-256 digest: d286111fb2f4c04ce17050b7d068e5954236d71bfc4452d11d4d4bdc064697c6
//Signer #1 certificate SHA-1 digest: b62a3b6096152d1bfb1d839d0a594350a8b2ee9d
//Signer #1 certificate MD5 digest: 107ce01259eec8aed717c5cce09ccab3


// Apksigner
//Number of signers: 1
//Verified using v1 scheme (JAR signing): true
//Verified using v2 scheme (APK Signature Scheme v2): true
//Verified using v3 scheme (APK Signature Scheme v3): false
//Verified using v3.1 scheme (APK Signature Scheme v3.1): false
//Verified using v4 scheme (APK Signature Scheme v4): false
