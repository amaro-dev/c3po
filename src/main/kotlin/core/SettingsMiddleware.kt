package core

import Settings
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import java.io.File
import java.util.Properties

class SettingsMiddleware(
    private val resourcesPath: File,
    private val settingsFile: String,
) : IMiddleware<AppState> {
    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.LoadSettings -> {
                val props = Properties()
                val propsFile = File(resourcesPath, settingsFile)
                if (propsFile.exists()) {
                    props.load(File(resourcesPath, settingsFile).inputStream())
                    processor.reduce(Action.LoadSettingsIntoState(props))
                    processor.perform(Action.RefreshDevices)
                } else {
                    processor.reduce(Action.SettingsNotFound)
                }
            }

            is Action.ChangeSettingsProperty -> {
                val props = state.settings.clone() as Properties
                props.setProperty(action.key, action.value)
                processor.reduce(Action.LoadSettingsIntoState(props))
                if (action.key == Settings.ADB_PATH_PROP) processor.perform(Action.RefreshDevices)
            }

            is Action.SaveSettings -> {
                val settingsFile = File(resourcesPath, settingsFile)
                if (!settingsFile.exists()) settingsFile.createNewFile()
                state.settings.store(settingsFile.outputStream(), null)
            }
        }
    }
}
