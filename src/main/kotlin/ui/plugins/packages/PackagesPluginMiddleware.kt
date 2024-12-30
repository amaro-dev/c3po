package ui.plugins.packages

import commands.ClearDataCommand
import commands.CommandExecutor
import commands.Error
import commands.ListPackagesCommand
import commands.StopAppCommand
import commands.Success
import commands.UninstallAppCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import ui.plugins.PluginMiddleware

class PackagesPluginMiddleware(
    pluginName: String,
    executor: CommandExecutor,
) : PluginMiddleware(pluginName, executor) {
    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.List,
                -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListPackagesCommand(), state, processor) {
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, it, searchTerm),
                    )
                }
            }

            is PackagesPlugin.Actions.Stop -> {
                execute(StopAppCommand(action.packageInfo), state, processor) {
                    processor.reduce(Action.SetCommandCompleted)
                }
            }

            is PackagesPlugin.Actions.Uninstall -> {
                execute(UninstallAppCommand(action.packageInfo), state, processor) {
                    when (it) {
                        is Success -> processor.perform(PackagesPlugin.Actions.List)
                        is Error ->
                            processor.reduce(Action.SetCommandError(it.message))
                    }
                }
            }

            is PackagesPlugin.Actions.ClearData -> {
                execute(ClearDataCommand(action.packageInfo), state, processor) {
                    processor.reduce(Action.SetCommandCompleted)
                }
            }
        }
    }
}
