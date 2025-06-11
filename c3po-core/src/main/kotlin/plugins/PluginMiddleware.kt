package plugins

import Settings
import commands.AdbCommand
import commands.CommandExecutor
import core.AppState
import dev.amaro.sonic.AsyncMiddlewareBase

abstract class PluginMiddleware(
    protected val pluginName: String,
) : AsyncMiddlewareBase<AppState>() {

    protected suspend fun <T> execute(
        command: AdbCommand<T>,
        state: AppState,
        executor: CommandExecutor
    ): Result<T> {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        return state.currentDevice?.run {
            executor.go(command, adbPath, this@run)
        } ?: Result.failure(IllegalArgumentException())
    }
}
