package plugins.permissions

import commands.CommandExecutor
import commands.ListDeclaredPermissions
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware

class PermissionsPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin, PermissionsPlugin.Actions.List -> {
                execute(ListDeclaredPermissions(), state, executor).handle(processor) {
                    processor.reduce(Action.DeliverPluginResult(pluginName, it))
                }
            }
        }
    }
}
