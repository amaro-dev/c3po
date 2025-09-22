package core.middleware

import Settings
import core.command.CommandExecutor
import core.debug
import core.facade.USBDeviceMonitor
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.AsyncMiddlewareBase
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Middleware that monitors USB device connections and triggers appropriate actions
 * when devices are physically connected or disconnected
 */
class USBMonitorMiddleware(
    private val executor: CommandExecutor,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AsyncMiddlewareBase<AppState>(scope) {

    private val usbMonitor = USBDeviceMonitor(executor, scope)
    private var isMonitoring = false

    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP) ?: ""

        when (action) {
            is Action.StartUSBMonitoring -> {
                if (!isMonitoring && adbPath.isNotBlank()) {
                    startUSBMonitoring(adbPath, processor)
                }
            }

            is Action.StopUSBMonitoring -> {
                stopUSBMonitoring()
            }

            is Action.ILoadSettingsIntoState -> {
                // Start USB monitoring after settings are applied, if ADB path is available
                val newAdbPath = state.settings.getProperty(Settings.ADB_PATH_PROP) ?: ""
                if (!isMonitoring && newAdbPath.isNotBlank()) {
                    debug("Starting USB monitoring after settings loaded")
                    startUSBMonitoring(newAdbPath, processor)
                }
            }

            is Action.ChangeSettingsProperty -> {
                if (action.key == Settings.ADB_PATH_PROP && action.value.isNotBlank()) {
                    if (isMonitoring) {
                        debug("Restarting USB monitoring with new ADB path")
                        stopUSBMonitoring()
                    } else {
                        debug("Starting USB monitoring with new ADB path")
                    }
                    startUSBMonitoring(action.value, processor)
                }
            }
        }
    }

    private fun startUSBMonitoring(adbPath: String, processor: IProcessor<AppState>) {
        debug("Starting USB device monitoring with path: $adbPath")

        usbMonitor.startBackgroundMonitoring(adbPath) { event ->
            // Process USB events directly since we're already in an async context
            when (event) {
                is USBDeviceMonitor.USBEvent.DeviceConnected -> {
                    debug("USB device connected: ${event.device.name}")
                    // Only refresh devices if this is a new device, not just a reconnection
                    processor.perform(Action.RefreshDevices)
                }

                is USBDeviceMonitor.USBEvent.DeviceDisconnected -> {
                    debug("USB device physically disconnected: ${event.device.name}")
                    // This is a real physical disconnection, clear device and reset plugins
                    processor.reduce(Action.ClearDevice)
                    processor.reduce(Action.ClearPlugins)
                }

                is USBDeviceMonitor.USBEvent.DeviceListUpdated -> {
                    debug("USB device list updated: ${event.devices.size} devices")
                    // Optionally update device list without clearing plugins
                    processor.reduce(Action.DeliverDevices(event.devices))
                }
            }
        }

        isMonitoring = true
    }

    private fun stopUSBMonitoring() {
        debug("Stopping USB device monitoring")
        usbMonitor.stopMonitoring()
        isMonitoring = false
    }

}
