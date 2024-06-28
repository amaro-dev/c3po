package models

data class IntentInfo(
    val intent: String,
    val hasExtras: Boolean,
    val flags: Long?,
    val packageName: String?,
    val dat: String?,
    val cmp: String?
)
