package core.model

enum class UpdateState {
    NoUpdate,
    CheckingForUpdate,
    UpdateAvailable,
    UpdateDismissed,
    Downloading,
    DownloadComplete,
    InstallReady,
    Installing,
    Error
}