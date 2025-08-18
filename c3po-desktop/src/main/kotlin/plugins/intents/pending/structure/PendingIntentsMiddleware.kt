package plugins.intents.pending.structure

import core.command.CommandExecutor
import core.command.ListPendingActivityIntentsCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import plugins.PluginMiddleware
import plugins.intents.pending.definition.PendingIntentsPlugin

class PendingIntentsMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
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
