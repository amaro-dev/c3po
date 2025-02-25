package plugins.packages

import plugins.signature.SignatureInfo
import plugins.signature.Signer

class SignatureResponseParser() {

    fun parse(response: List<String>): SignatureInfo {
        return SignatureInfo(
            Signer.fromParams(*response[1].split(",").toTypedArray()),
            response[3],
            response[4],
            response[5],
        )
    }

}
