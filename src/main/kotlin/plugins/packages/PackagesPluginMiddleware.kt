package plugins.packages

import commands.ClearDataCommand
import commands.CommandExecutor
import commands.ListPackagesCommand
import commands.StopAppCommand
import commands.UninstallAppCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import models.AppPackage
import plugins.PluginMiddleware

class PackagesPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.List,
                -> {
                execute(ListPackagesCommand(), state, executor).handle(processor) {
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, it),
                    )
                }
            }

            is PackagesPlugin.Actions.Stop -> {
                execute(StopAppCommand(action.packageInfo), state, executor)
                    .onSuccess { processor.perform(PackagesPlugin.Actions.List) }
                    .handle(processor)

            }

            is PackagesPlugin.Actions.Uninstall -> {
                execute(UninstallAppCommand(action.packageInfo), state, executor).handle(processor)
            }

            is PackagesPlugin.Actions.ExtractKey -> {
                processor.perform(
                    Action.SendSocketRequest(
                        PackagesPlugin.EXTRACT_KEY_INSTRUCTION,
                        action.packageInfo.packageName,
                    ),
                )
            }

            is PackagesPlugin.Actions.ClearData -> {
                execute(ClearDataCommand(action.packageInfo), state, executor).handle(processor)
            }

            is Action.DeliverSocketResponse -> {
                if (action.reference.command == PackagesPlugin.EXTRACT_KEY_INSTRUCTION) {
                    val signature = SignatureResponseParser().parse(action.content)
                    val response = state.windows[pluginName]?.result?.map {
                        if ((it as AppPackage).packageName == action.reference.arg)
                            it.copy(signerInfo = signature)
                        else
                            it
                    } ?: emptyList()
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, response),
                    )
                }
            }
        }
    }
}
