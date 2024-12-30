package ui.plugins.intents.pending

import commands.CommandExecutor
import commands.ListPendingActivityIntentsCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import ui.plugins.PluginMiddleware

class PendingIntentsMiddleware(
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
            PendingIntentsPlugin.Actions.List,
                -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListPendingActivityIntentsCommand(), state, processor) { results ->
                    processor.reduce(
                        Action.DeliverPluginResult(
                            pluginName,
                            results.groupBy { it.packageName }.toList(),
                            searchTerm,
                        ),
                    )
                }
            }
        }
    }
}
