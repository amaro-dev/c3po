package models

data class PendingIntent(
    val entryId: String,
    val packageName: String,
    val flags: Long,
    val type: String,
    val intent: IntentInfo,
    val requestCode: Int?,
)
