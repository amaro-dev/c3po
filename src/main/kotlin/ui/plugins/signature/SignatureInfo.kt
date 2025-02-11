package ui.plugins.signature

data class SignatureInfo(
    val signer: Signer,
    val sha256Digest: String,
    val shar1Digest: String,
    val md5Digest: String
)
