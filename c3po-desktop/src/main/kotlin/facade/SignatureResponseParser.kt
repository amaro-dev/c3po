package facade

import models.SignatureInfo
import models.Signer

class SignatureResponseParser {
    fun parse(response: List<String>): SignatureInfo =
        SignatureInfo(
            Signer.fromParams(*response[1].split(",").toTypedArray()),
            response[3],
            response[4],
            response[5],
        )
}
