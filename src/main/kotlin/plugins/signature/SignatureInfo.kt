package plugins.signature

data class SignatureInfo(
    val signer: Signer,
    val sha256Digest: String,
    val sha1Digest: String,
    val md5Digest: String
)
