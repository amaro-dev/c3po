package core.middleware

import Settings
import core.command.CommandExecutor
import core.command.ListDevicesCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor

class DeviceMiddleware(
    private val executor: CommandExecutor,
) : AsyncMiddlewareBase<AppState>() {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)

        if (action is Action.CommandAction) processor.reduce(Action.SetCommandRunning)
        when (action) {
            is Action.RefreshDevices -> {
                processor.reduce(Action.ClearDevice)
                executor.go(ListDevicesCommand(), adbPath).handle(processor) { devices ->
                    processor.reduce(Action.DeliverDevices(devices))
                    // Select the device if it's the only one available
                    if (devices.size == 1) {
                        processor.perform(Action.SelectDevice(devices[0]))
                    }
                }
            }
        }
    }
}
