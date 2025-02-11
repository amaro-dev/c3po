package ui.plugins.signature

data class AndroidPackageReport(
    val signature: SignatureInfo,
    val signerCount: Int,
)
