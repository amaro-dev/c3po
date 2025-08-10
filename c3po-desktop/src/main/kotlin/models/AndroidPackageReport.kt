package models

import facade.BoolState

data class AndroidPackageReport(
    val filePath: String,
    val signature: SignatureInfo,
    val signerCount: Int,
    val isV1Compliant: BoolState,
    val isV2Compliant: BoolState,
    val isV3Compliant: BoolState,
    val isV31Compliant: BoolState,
    val isV4Compliant: BoolState,
)
