package core.middleware

import Settings
import core.facade.AdbFinder
import core.facade.SettingsRepository
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Properties

class SettingsMiddleware(
    private val settingsRepository: SettingsRepository,
    private val adbFinder: AdbFinder,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AsyncMiddlewareBase<AppState>(scope) {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.LoadSettings -> {
                settingsRepository
                    .load()
                    .onSuccess {
                        processor.reduce(Action.LoadSettingsIntoState(it))
                        processor.perform(Action.RefreshDevices)
                    }.onFailure {
                        processor.reduce(Action.SettingsNotFound)
                    }
            }

            is Action.ChangeSettingsProperty -> {
                val props = state.settings.clone() as Properties
                props.setProperty(action.key, action.value)
                processor.reduce(Action.LoadSettingsIntoState(props))
                processor.perform(Action.SaveSettings)
                if (action.key == Settings.ADB_PATH_PROP) processor.perform(Action.RefreshDevices)
            }

            is Action.SaveSettings -> {
                settingsRepository.save(state.settings).handle(processor)
            }

            is Action.SearchAdbPath -> {
                // Start scoped loading for global UI and local field state
                processor.reduce(Action.SetAdbPathSearching)
                processor.reduce(Action.SetCommandRunning)
                val result = adbFinder.find()
                if (result.isSuccess) {
                    val adbPath = result.getOrNull()!!.trim()
                    processor.reduce(Action.SetAdbPathSearchResult(adbPath))
                    // Auto-save the found ADB path
                    processor.perform(Action.ChangeSettingsProperty(Settings.ADB_PATH_PROP, adbPath))
                } else {
                    processor.reduce(
                        Action.SetAdbPathSearchError(
                            result.exceptionOrNull()?.message
                                ?: "ADB not found. Please install Android SDK or set path manually."
                        )
                    )
                }
                // Stop global loading (we did not set SetCommandError, so Completed is correct)
                processor.reduce(Action.SetCommandCompleted)
            }
        }
    }
}
