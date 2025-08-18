package core.facade

import core.command.CommandExecutor
import core.command.ListDevicesCommand
import core.debug
import core.model.AdbDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Monitors USB device connections using ADB polling
 * Future enhancement: Replace with native IOKit integration for real-time USB events
 */
class USBDeviceMonitor(
    private val executor: CommandExecutor,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val POLLING_INTERVAL_MS = 3000L // 3 seconds
        private const val DEVICE_STABILITY_CHECK_COUNT = 2 // Require device to be present for 2 polls
    }

    private var monitoringJob: Job? = null
    private var lastKnownDevices: List<AdbDevice> = emptyList()
    private val deviceStabilityMap = mutableMapOf<String, Int>()

    sealed class USBEvent {
        data class DeviceConnected(val device: AdbDevice) : USBEvent()
        data class DeviceDisconnected(val device: AdbDevice) : USBEvent()
        data class DeviceListUpdated(val devices: List<AdbDevice>) : USBEvent()
    }

    /**
     * Start monitoring USB devices
     */
    fun startMonitoring(adbPath: String): Flow<USBEvent> = flow {
        debug("Starting USB device monitoring with ADB polling")

        while (true) {
            try {
                val currentDevices = executor.go(ListDevicesCommand(), adbPath)
                    .getOrElse { emptyList() }

                // Check for device stability (device must be present for multiple polls)
                val stableDevices = filterStableDevices(currentDevices)

                // Compare with last known devices to detect changes
                val events = detectDeviceChanges(stableDevices)

                // Emit events for device changes
                events.forEach { event ->
                    debug("USB Event: $event")
                    emit(event)
                }

                // Always emit updated device list
                if (stableDevices != lastKnownDevices) {
                    emit(USBEvent.DeviceListUpdated(stableDevices))
                    lastKnownDevices = stableDevices
                }

            } catch (e: Exception) {
                debug("Error during USB device monitoring: ${e.message}")
            }

            delay(POLLING_INTERVAL_MS)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Filter devices that have been stable across multiple polling cycles
     */
    private fun filterStableDevices(currentDevices: List<AdbDevice>): List<AdbDevice> {
        // Update stability count for current devices
        currentDevices.forEach { device ->
            deviceStabilityMap[device.id] = (deviceStabilityMap[device.id] ?: 0) + 1
        }

        // Remove devices that are no longer present
        val currentDeviceIds = currentDevices.map { it.id }.toSet()
        deviceStabilityMap.keys.removeAll { it !in currentDeviceIds }

        // Return only devices that have been stable for required count
        return currentDevices.filter { device ->
            (deviceStabilityMap[device.id] ?: 0) >= DEVICE_STABILITY_CHECK_COUNT
        }
    }

    /**
     * Detect what changed between current and last known devices
     */
    private fun detectDeviceChanges(currentDevices: List<AdbDevice>): List<USBEvent> {
        val events = mutableListOf<USBEvent>()

        val currentDeviceIds = currentDevices.map { it.id }.toSet()
        val lastKnownDeviceIds = lastKnownDevices.map { it.id }.toSet()

        // Detect new devices (connected)
        val newDevices = currentDevices.filter { it.id !in lastKnownDeviceIds }
        newDevices.forEach { device ->
            events.add(USBEvent.DeviceConnected(device))
        }

        // Detect removed devices (disconnected)
        val removedDevices = lastKnownDevices.filter { it.id !in currentDeviceIds }
        removedDevices.forEach { device ->
            events.add(USBEvent.DeviceDisconnected(device))
        }

        return events
    }

    /**
     * Start monitoring in background
     */
    fun startBackgroundMonitoring(adbPath: String, onEvent: (USBEvent) -> Unit) {
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            startMonitoring(adbPath).collect { event ->
                onEvent(event)
            }
        }
        debug("USB device monitoring started in background")
    }

    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        deviceStabilityMap.clear()
        lastKnownDevices = emptyList()
        debug("USB device monitoring stopped")
    }

    /**
     * Check if a specific device is currently connected
     */
    suspend fun isDeviceConnected(deviceId: String, adbPath: String): Boolean {
        return try {
            val devices = executor.go(ListDevicesCommand(), adbPath).getOrElse { emptyList() }
            devices.any { it.id == deviceId }
        } catch (e: Exception) {
            debug("Error checking device connectivity: ${e.message}")
            false
        }
    }

    /**
     * Get current device statistics
     */
    fun getMonitoringStats(): String {
        return "Monitoring: ${monitoringJob?.isActive == true}, " +
                "Known devices: ${lastKnownDevices.size}, " +
                "Stability tracking: ${deviceStabilityMap.size}"
    }
}