package plugins.intents.pending

import commands.CommandExecutor
import commands.ListPendingActivityIntentsCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware

class PendingIntentsMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            PendingIntentsPlugin.Actions.List,
                -> {
                execute(ListPendingActivityIntentsCommand(), state, executor).handle(processor) { results ->
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, results),
                    )
                }
            }
        }
    }
}
