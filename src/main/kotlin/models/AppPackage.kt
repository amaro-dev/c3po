package models

import plugins.signature.SignatureInfo

data class AppPackage(
    val packageName: String,
    val versionName: String = "",
    val versionCode: Int = -1,
    val targetSdk: Int = -1,
    val signerInfo: SignatureInfo? = null
)
