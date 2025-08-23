package core.middleware

import core.facade.UpdateService
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor

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
        // TODO: Implement download logic in later phases
        // For now, just acknowledge the action
        processor.reduce(Action.UpdateError("Download functionality not yet implemented"))
    }
}