package ui.plugins

import Settings
import commands.AdbCommand
import commands.CommandExecutor
import core.AppState
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class PluginMiddleware(
    protected val pluginName: String,
    private val executor: CommandExecutor
) : IMiddleware<AppState> {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    protected fun <T> execute(
        command: AdbCommand<T>,
        state: AppState,
        processor: IProcessor<AppState>,
        onComplete: (T) -> Unit
    ) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        state.currentDevice?.run {
            coroutineScope.launch {
                val result = executor.go(command, processor, adbPath, this@run)
                result?.run { onComplete(this) }
            }
        }
    }

}
