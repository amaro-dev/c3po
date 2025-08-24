package core.middleware

import core.facade.update.UpdateChecker
import core.facade.update.UpdateDownloader
import core.facade.update.UpdateInstaller
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import java.io.File
import java.io.IOException

class UpdateMiddleware(
    private val updateChecker: UpdateChecker,
    private val updateDownloader: UpdateDownloader,
    private val updateInstaller: UpdateInstaller
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
                handleDownloadUpdate(action, processor)
            }

            is Action.DismissUpdate -> {
                processor.reduce(Action.DismissUpdate)
            }

            is Action.InstallUpdate -> {
                handleInstallUpdate(action, processor)
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

            // Validate update URL format
            if (!updateChecker.isValidUrl(updateUrl)) {
                processor.reduce(Action.UpdateError("Invalid update URL configured: $updateUrl"))
                return
            }

            // Get current version - for now hardcoded, could be made configurable
            val currentVersion = "2.0.1"

            // Check for updates with timeout and retries
            val updateInfo = updateChecker.withRetry(maxRetries = 3, delayMs = 1000) {
                updateChecker.checkForUpdates(currentVersion, updateUrl)
            }
            
            // Additional validation - ensure we got a valid newer version
            if (updateInfo != null) {
                if (!updateChecker.validateVersion(updateInfo.version, currentVersion)) {
                    // Log but don't show error - this is expected behavior for same/older versions
                    processor.reduce(Action.UpdateCheckComplete(null))
                    return
                }
            }

            processor.reduce(Action.UpdateCheckComplete(updateInfo))

        } catch (e: IllegalArgumentException) {
            processor.reduce(Action.UpdateError("Invalid update configuration: ${e.message}"))
        } catch (e: IOException) {
            processor.reduce(Action.UpdateError("Network error while checking for updates. Please check your internet connection."))
        } catch (e: InterruptedException) {
            processor.reduce(Action.UpdateError("Update check was cancelled"))
        } catch (e: Exception) {
            processor.reduce(Action.UpdateError("Failed to check for updates: ${e.message}"))
        }
    }

    private suspend fun handleDownloadUpdate(
        action: Action.DownloadUpdate,
        processor: IProcessor<AppState>
    ) {
        var downloadedFile: File? = null
        try {
            // Get current version for validation
            val currentVersion = "2.0.1" // Same as in handleCheckForUpdate
            
            // Enhanced version validation before download
            if (!updateChecker.validateVersion(action.updateInfo.version, currentVersion)) {
                processor.reduce(Action.UpdateError("Invalid version: ${action.updateInfo.version} is not newer than current version $currentVersion"))
                return
            }

            processor.reduce(action)

            // Start download with timeout and progress tracking
            updateDownloader.downloadUpdate(action.updateInfo).collect { progress ->
                // Update progress
                processor.reduce(Action.UpdateDownloadProgress(progress.progress))
                
                // Handle download completion
                if (progress.progress >= 100) {
                    val downloadDir = File(System.getProperty("java.io.tmpdir"), "c3po-updates")
                    downloadedFile = File(downloadDir, "c3po-${action.updateInfo.version}.dmg")

                    // Validate downloaded file
                    val validationResult = updateDownloader.validateDownloadedFile(
                        downloadedFile!!,
                        action.updateInfo.checksum
                    )

                    when (validationResult) {
                        is UpdateDownloader.ValidationResult.Success -> {
                            processor.reduce(Action.UpdateDownloadComplete(downloadedFile!!.absolutePath))
                        }

                        is UpdateDownloader.ValidationResult.Failed -> {
                            processor.reduce(Action.UpdateError(validationResult.message))
                            cleanupDownloadedFile(downloadedFile!!)
                            return@collect
                        }
                    }
                }
            }

        } catch (e: IllegalArgumentException) {
            processor.reduce(Action.UpdateError("Download configuration error: ${e.message}"))
            downloadedFile?.let { cleanupDownloadedFile(it) }
        } catch (e: IOException) {
            processor.reduce(Action.UpdateError("Network error during download. Please check your internet connection and try again."))
            downloadedFile?.let { cleanupDownloadedFile(it) }
        } catch (e: InterruptedException) {
            processor.reduce(Action.UpdateError("Download was cancelled"))
            downloadedFile?.let { cleanupDownloadedFile(it) }
        } catch (e: Exception) {
            processor.reduce(Action.UpdateError("Download failed: ${e.message}"))
            downloadedFile?.let { cleanupDownloadedFile(it) }
        }
    }

    private suspend fun handleInstallUpdate(
        action: Action.InstallUpdate,
        processor: IProcessor<AppState>
    ) {
        try {
            val installFile = File(action.filePath)
            val installResult = updateInstaller.installUpdate(installFile)

            if (installResult.success) {
                // Clean up downloaded file after successful installation
                cleanupDownloadedFile(installFile)

                processor.reduce(Action.UpdateInstallComplete)

                // If installation requires restart, initiate graceful shutdown
                if (installResult.requiresRestart) {
                    // Give UI time to show success message
                    kotlinx.coroutines.delay(2000)
                    processor.reduce(Action.RestartApplication)
                }
            } else {
                processor.reduce(Action.UpdateError(installResult.message ?: "Installation failed"))
                // Keep downloaded file for retry attempts
            }
        } catch (e: Exception) {
            processor.reduce(Action.UpdateError("Installation error: ${e.message}"))
        }
    }

    private fun cleanupDownloadedFile(file: File) {
        try {
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log but don't throw - this is cleanup
        }
    }
}