package plugins.permissions.structure

import core.command.CommandExecutor
import core.command.ListDeclaredPermissions
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware
import plugins.permissions.definition.PermissionsPlugin

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
