import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import facade.SignatureResponseParser
import models.SignatureInfo
import models.Signer
import org.junit.jupiter.api.Test

class SignatureResponseParserTest {
    @Test
    fun `Parse full response`() {
        val response = listOf(
            "1",
            "CN=Newland Payment Verify,OU=Newland Payment,O=Newland Payment Technology Co. Ltd,L=FZ,ST=FJ,C=CN",
            "AlgName",
            "SHA256",
            "SHA1",
            "MD5"
        )

        assertThat(SignatureResponseParser().parse(response)).all {
            prop(SignatureInfo::signer).all {
                prop(Signer::organization).isEqualTo("Newland Payment Technology Co. Ltd")
                prop(Signer::organizationUnit).isEqualTo("Newland Payment")
                prop(Signer::state).isEqualTo("FJ")
                prop(Signer::locality).isEqualTo("FZ")
                prop(Signer::countryCode).isEqualTo("CN")
                prop(Signer::commonName).isEqualTo("Newland Payment Verify")
            }
            prop(SignatureInfo::sha256Digest).isEqualTo("SHA256")
            prop(SignatureInfo::sha1Digest).isEqualTo("SHA1")
            prop(SignatureInfo::md5Digest).isEqualTo("MD5")
        }

    }


}
