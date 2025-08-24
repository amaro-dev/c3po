package core.middleware

import core.facade.UpdateService
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import java.io.File

class UpdateMiddleware(
    private val updateService: UpdateService
) : AsyncMiddlewareBase<AppState>() {

    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.CheckForUpdate -> {
                handleCheckForUpdate(state, processor)
            }

            is Action.DownloadUpdate -> {
                handleDownloadUpdate(action, state, processor)
            }

            is Action.DismissUpdate -> {
                processor.reduce(Action.DismissUpdate)
            }

            else -> {
                // Not an update action, ignore
            }
        }
    }

    private suspend fun handleCheckForUpdate(
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        try {
            // Check if auto-updates are enabled
            val autoUpdateEnabled = state.settings.getProperty("update.auto.enabled", "true").toBoolean()
            if (!autoUpdateEnabled) {
                processor.reduce(Action.UpdateError("Auto-updates are disabled"))
                return
            }

            // Get update URL from settings or use default
            val updateUrl = state.settings.getProperty(
                "update.check.url",
                "https://api.github.com/repos/amaro-dev/c3po/releases/latest"
            )

            // Get current version - for now hardcoded, could be made configurable
            val currentVersion = "2.0.1"

            // Check for updates
            val updateInfo = updateService.checkForUpdates(currentVersion, updateUrl)
            
            // Additional validation - ensure we got a valid newer version
            if (updateInfo != null) {
                if (!updateService.validateVersion(updateInfo.version, currentVersion)) {
                    // Log but don't show error - this is expected behavior for same/older versions
                    processor.reduce(Action.UpdateCheckComplete(null))
                    return
                }
            }

            processor.reduce(Action.UpdateCheckComplete(updateInfo))

        } catch (e: Exception) {
            processor.reduce(Action.UpdateError("Failed to check for updates: ${e.message}"))
        }
    }

    private suspend fun handleDownloadUpdate(
        action: Action.DownloadUpdate,
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        try {
            // Get current version for validation
            val currentVersion = "2.0.1" // Same as in handleCheckForUpdate
            
            // Enhanced version validation before download
            if (!updateService.validateVersion(action.updateInfo.version, currentVersion)) {
                processor.reduce(Action.UpdateError("Invalid version: ${action.updateInfo.version} is not newer than current version $currentVersion"))
                return
            }

            // Start download
            updateService.downloadUpdate(action.updateInfo).collect { progress ->
                // Update progress
                processor.reduce(Action.UpdateDownloadProgress(progress.progress))
                
                // Handle download completion
                if (progress.progress >= 100) {
                    val downloadDir = File(System.getProperty("java.io.tmpdir"), "c3po-updates")
                    val downloadedFile = File(downloadDir, "c3po-${action.updateInfo.version}.dmg")
                    
                    // Verify checksum if available
                    if (!updateService.verifyChecksum(downloadedFile, action.updateInfo.checksum)) {
                        processor.reduce(Action.UpdateError("Downloaded file failed checksum verification"))
                        // Clean up invalid file
                        if (downloadedFile.exists()) {
                            downloadedFile.delete()
                        }
                        return@collect
                    }
                    
                    processor.reduce(Action.UpdateDownloadComplete(downloadedFile.absolutePath))
                }
            }

        } catch (e: Exception) {
            processor.reduce(Action.UpdateError("Download failed: ${e.message}"))
        }
    }
}