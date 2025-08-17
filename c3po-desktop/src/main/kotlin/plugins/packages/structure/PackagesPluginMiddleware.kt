package plugins.packages.structure

import core.command.ClearDataCommand
import core.command.CommandExecutor
import core.command.ListPackagesCommand
import core.command.StopAppCommand
import core.command.UninstallAppCommand
import core.facade.SignatureResponseParser
import core.model.Action
import core.model.AppPackage
import core.model.AppState
import core.model.SleepState
import debug
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware
import plugins.packages.definition.PackagesPlugin
import toBool
import update

class PackagesPluginMiddleware(
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
            PackagesPlugin.Actions.List,
                -> {
                execute(ListPackagesCommand(), state, executor)
                    .handle(processor) {
                        processor.reduce(
                            Action.DeliverPluginResult(pluginName, it),
                        )
                    }
            }

            is PackagesPlugin.Actions.Stop -> {
                execute(StopAppCommand(action.packageInfo), state, executor)
                    .handle(processor)
            }

            is PackagesPlugin.Actions.Uninstall -> {
                execute(UninstallAppCommand(action.packageInfo), state, executor)
                    .onSuccess { processor.perform(PackagesPlugin.Actions.List) }
                    .handle(processor)
            }

            is PackagesPlugin.Actions.ExtractKey -> {
                processor.perform(
                    Action.SendSocketRequest(
                        PackagesPlugin.EXTRACT_KEY_INSTRUCTION,
                        action.packageInfo.packageName,
                    ),
                )
            }

            is PackagesPlugin.Actions.CheckAsleep -> {
                processor.perform(
                    Action.SendSocketRequest(
                        PackagesPlugin.CHECK_ASLEEP_INSTRUCTION,
                        action.packageInfo.packageName,
                    ),
                )
            }

            is PackagesPlugin.Actions.ClearData -> {
                execute(ClearDataCommand(action.packageInfo), state, executor).handle(processor)
            }

            is Action.DeliverSocketResponse -> {
                if (action.reference.command == PackagesPlugin.EXTRACT_KEY_INSTRUCTION) {
                    debug("Received signature response for package: ${action.reference.arg}")
                    debug("Response content: ${action.content}")
                    try {
                        val signature = SignatureResponseParser().parse(action.content)
                        debug("Parsed signature: $signature")
                        val response =
                            updateByPackageName(state, action.reference.arg!!) {
                                (it).copy(signerInfo = signature)
                            }
                        debug("Updated packages count: ${response.size}")
                        processor.reduce(
                            Action.DeliverPluginResult(pluginName, response),
                        )
                    } catch (e: Exception) {
                        debug("Error parsing signature response: ${e.message}")
                        e.printStackTrace()
                    }
                } else if (action.reference.command == PackagesPlugin.CHECK_ASLEEP_INSTRUCTION) {
                    val result = if (action.content.first().toBool()) SleepState.Asleep else SleepState.Awake
                    val response =
                        updateByPackageName(state, action.reference.arg!!) {
                            it.copy(sleepState = result)
                        }
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, response),
                    )
                }
            }
        }
    }

    private fun updateByPackageName(
        state: AppState,
        packageName: String,
        transform: (AppPackage) -> AppPackage,
    ): List<AppPackage> =
        (
                state.windows[pluginName]
                    ?.result as? List<AppPackage>
                )?.update({ it.packageName == packageName }) { transform(it) }
            ?: emptyList()
}
