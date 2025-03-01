package core

import Settings
import debug
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import facade.CompanionCommander
import handle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class CompanionMiddleware(
    private val commander: CompanionCommander
) : AsyncMiddlewareBase<AppState>(CoroutineScope(Dispatchers.IO)) {


    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            // When a device is selected we must check for companion app
            is Action.SelectDevice -> {
                debug("Companion selected '${state.currentDevice}' and '${action.device}'")
                if (state.currentDevice != action.device)
                    (processor as IActionScheduler).schedule(Action.Companion.CheckInstalled)
            }

            is Action.Companion.Install -> {
                if (state.hasDeviceSet) {
                    processor.reduce(Action.SetCommandRunning)
                    commander.installAndPrepare(adbPath, state.currentDevice!!)
                        .onSuccess { processor.perform(Action.Companion.Connect) }
                        .handle(processor)
                }
            }


            is Action.Companion.CheckInstalled -> {
                state.currentDevice?.let { device ->
                    var status = CompanionState().setCheckedForPresence()
                    val isInstalled = commander.isInstalled(adbPath, device).getOrDefault(false)
                    if (isInstalled) {
                        status = status.setIsInstalled()
                        (processor as IActionScheduler).schedule(Action.Companion.Prepare)
                    } else {
                        (processor as IActionScheduler).schedule(Action.Companion.Install)
                    }
                    processor.reduce(Action.Companion.UpdateState(status))
                }
            }

            is Action.Companion.Prepare -> {
                if (state.currentDevice == null) return
                commander.prepare(adbPath, state.currentDevice)
                    .onSuccess { processor.perform(Action.Companion.Connect) }
                    .handle(processor)
            }

        }
    }

}
