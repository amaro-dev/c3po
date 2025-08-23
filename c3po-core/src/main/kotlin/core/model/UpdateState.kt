package core.model

enum class UpdateState {
    NoUpdate,
    CheckingForUpdate,
    UpdateAvailable,
    Downloading,
    DownloadComplete,
    Error
}