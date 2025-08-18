package core.middleware

import Settings
import core.facade.SettingsRepository
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import java.util.Properties

class SettingsMiddleware(
    private val settingsRepository: SettingsRepository,
) : IMiddleware<AppState> {
    override fun process(
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
        }
    }
}
