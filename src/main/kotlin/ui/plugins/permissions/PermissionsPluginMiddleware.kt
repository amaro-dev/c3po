package ui.plugins.permissions

import commands.CommandExecutor
import commands.ListDeclaredPermissions
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import ui.plugins.PluginMiddleware

class PermissionsPluginMiddleware(
    pluginName: String,
    executor: CommandExecutor,
) : PluginMiddleware(pluginName, executor) {
    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin, PermissionsPlugin.Actions.List -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListDeclaredPermissions(), state, processor) {
                    processor.reduce(Action.DeliverPluginResult(pluginName, it, searchTerm))
                }
            }
        }
    }
}
