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
                executor.go(ListDevicesCommand(), adbPath).handle(processor) { devices ->
                    // Only clear device if current device is no longer available
                    val currentDevice = state.currentDevice
                    val deviceStillAvailable = currentDevice != null && 
                        devices.any { it.id == currentDevice.id }
                    
                    if (!deviceStillAvailable) {
                        processor.reduce(Action.ClearDevice)
                    }
                    
                    processor.reduce(Action.DeliverDevices(devices))
                    
                    // Auto-select device only if no device is currently selected
                    if (state.currentDevice == null && devices.size == 1) {
                        processor.perform(Action.SelectDevice(devices[0]))
                        // Auto-select Device plugin immediately when device becomes available
                        processor.perform(Action.StartPlugin("DEVICE"))
                    }
                }
            }
        }
    }
}
