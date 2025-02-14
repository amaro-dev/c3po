package ui.plugins.intents.pending

import commands.CommandExecutor
import commands.ListPendingActivityIntentsCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import ui.plugins.PluginMiddleware

class PendingIntentsMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            PendingIntentsPlugin.Actions.List,
                -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListPendingActivityIntentsCommand(), state, executor).handle(processor) { results ->
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
