package plugins.activities.structure

import core.command.CommandExecutor
import core.command.ListActivitiesCommand
import core.command.SetLauncherCommand
import core.command.StartActivityCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import plugins.PluginMiddleware

class ActivitiesPluginMiddleware(
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
            plugins.activities.definition.ActivitiesPlugin.Actions.List,
                -> {
                execute(ListActivitiesCommand(), state, executor).handle(processor) {
                    processor.reduce(Action.DeliverPluginResult(pluginName, it))
                }
            }

            is plugins.activities.definition.ActivitiesPlugin.Actions.Launch -> {
                execute(StartActivityCommand(action.activityInfo, action.forDebug), state, executor)
                    .onSuccess {
                        processor.reduce(Action.SetSuccess("Activity '${action.activityInfo.fullPath}' started successfully"))
                    }
                    .handle(processor)
            }

            is plugins.activities.definition.ActivitiesPlugin.Actions.SetLauncher -> {
                execute(SetLauncherCommand(action.activityInfo), state, executor)
                    .onSuccess {
                        processor.reduce(Action.SetSuccess("Launcher set to '${action.activityInfo.fullPath}' successfully"))
                    }
                    .handle(processor)
            }

        }
    }
}