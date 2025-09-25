package plugins.broadcasts.structure

import core.command.CommandExecutor
import core.command.ListBroadcastActionsCommand
import core.command.SendBroadcastCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import plugins.PluginMiddleware
import plugins.broadcasts.definition.BroadcastsPlugin

class BroadcastsMiddleware(
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
            BroadcastsPlugin.Actions.List,
                -> {
                execute(ListBroadcastActionsCommand(), state, executor).handle(processor) { broadcasts ->
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, broadcasts),
                    )
                }
            }

            is BroadcastsPlugin.Actions.Send -> {
                execute(
                    SendBroadcastCommand(action.broadcastAction, action.extras),
                    state,
                    executor
                ).handle(processor) { result ->
                    // Show the result as a success or error message
                    if (result.contains("successfully") || result.contains("Broadcasting")) {
                        processor.reduce(Action.SetSuccess(result))
                    } else {
                        processor.reduce(Action.SetCommandError(result))
                    }
                }
            }
        }
    }
}