package core.model

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val checksum: String? = null,
    val releaseNotesUrl: String? = null
)