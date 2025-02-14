package ui.plugins.packages

import commands.ClearDataCommand
import commands.CommandExecutor
import commands.ListPackagesCommand
import commands.StopAppCommand
import commands.UninstallAppCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import ui.plugins.PluginMiddleware

class PackagesPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.List,
                -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListPackagesCommand(), state, executor).handle(processor) {
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, it, searchTerm),
                    )
                }
            }

            is PackagesPlugin.Actions.Stop -> {
                execute(StopAppCommand(action.packageInfo), state, executor).handle(processor)
            }

            is PackagesPlugin.Actions.Uninstall -> {
                execute(UninstallAppCommand(action.packageInfo), state, executor).handle(processor)
            }

            is PackagesPlugin.Actions.ClearData -> {
                execute(ClearDataCommand(action.packageInfo), state, executor).handle(processor)
            }
        }
    }
}
