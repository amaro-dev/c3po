package plugins.activities.structure

import core.command.CommandExecutor
import core.command.ListActivitiesCommand
import core.command.QueryLauncherActivitiesCommand
import core.command.SetLauncherCommand
import core.command.StartActivityCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
                // Composite operation: run listing and launcher query in parallel and merge results.
                // We avoid handle(processor) for sub-commands to control overall CommandStatus.
                coroutineScope {
                    val activitiesDeferred = async { execute(ListActivitiesCommand(), state, executor) }
                    val launcherDeferred = async { execute(QueryLauncherActivitiesCommand(), state, executor) }

                    val activitiesResult = activitiesDeferred.await()

                    if (activitiesResult.isFailure) {
                        val message = activitiesResult.exceptionOrNull()?.message ?: "Failed to list activities"
                        processor.reduce(Action.SetCommandError(message))
                        return@coroutineScope
                    }

                    val activities = activitiesResult.getOrThrow()

                    val launcherResult = launcherDeferred.await()
                    val launcherSet: Set<String> =
                        if (launcherResult.isSuccess) launcherResult.getOrThrow().map { act -> act.fullPath }.toSet()
                        else emptySet()

                    val merged =
                        activities.map { info -> info.copy(isLauncherCapable = launcherSet.contains(info.fullPath)) }

                    processor.reduce(Action.DeliverPluginResult(pluginName, merged))
                    processor.reduce(Action.SetCommandCompleted)
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
