package core

import Settings
import commands.DeviceInfoCommand
import commands.ListDevicesCommand
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

class CommandMiddleware : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.RefreshDevices -> {
                val devices = ListDevicesCommand.run(adbPath)
                processor.reduce(Action.DeliverDevices(devices))
                if (devices.size == 1) processor.perform(Action.SelectDevice(devices[0]))
            }

            is Action.SelectDevice -> {
                val deviceInfo = DeviceInfoCommand(action.device).run(adbPath)
                processor.reduce(Action.SelectDevice(action.device.copy(details = deviceInfo)))
                val fixedList = state.devices.map {
                    if (it.id == action.device.id)
                        it.copy(details = deviceInfo)
                    else
                        it
                }
                processor.reduce(Action.DeliverDevices(fixedList))
            }
        }
    }
}
