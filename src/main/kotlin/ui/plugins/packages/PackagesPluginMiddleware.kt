package ui.plugins.packages

import commands.ClearDataCommand
import commands.ListPackagesCommand
import commands.StopAppCommand
import commands.UninstallAppCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

class PackagesPluginMiddleware(private val name: String) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.List -> {
                state.currentDevice?.run {
                    processor.reduce(
                        Action.DeliverPluginResult(name, ListPackagesCommand(this).run())
                    )
                }
            }

            is PackagesPlugin.Actions.Stop -> {
                state.currentDevice?.run {
                    StopAppCommand(this, action.packageInfo).run()
                }
            }

            is PackagesPlugin.Actions.Uninstall -> {
                state.currentDevice?.run {
                    UninstallAppCommand(this, action.packageInfo).run()
                }
            }

            is PackagesPlugin.Actions.ClearData -> {
                state.currentDevice?.run {
                    ClearDataCommand(this, action.packageInfo).run()
                }
            }

            else -> null
        }?.run { }
    }

}
