package device

import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.AdbDevice

/**
 * Device manager that integrates with Android Studio's built-in device management.
 * Uses Android Studio's ADB infrastructure instead of maintaining our own.
 */
class AndroidStudioDeviceManager(private val project: Project) {

    companion object {
        private val LOG = Logger.getInstance(AndroidStudioDeviceManager::class.java)
    }

    /**
     * Get connected devices using Android Studio's device management
     */
    suspend fun getConnectedDevices(): List<AdbDevice> = withContext(Dispatchers.IO) {
        try {
            val adbService = AdbService.getInstance()
            val debugBridge = adbService.getDebugBridge(project).get()

            val devices = debugBridge?.devices?.map { device ->
                convertToAdbDevice(device)
            } ?: emptyList()

            LOG.info("Found ${devices.size} connected devices")
            devices
        } catch (e: Exception) {
            LOG.error("Failed to get connected devices", e)
            emptyList()
        }
    }

    /**
     * Execute shell command on device using Android Studio's ADB integration
     */
    suspend fun executeCommand(device: AdbDevice, command: String): String = withContext(Dispatchers.IO) {
        try {
            val adbService = AdbService.getInstance()
            val debugBridge = adbService.getDebugBridge(project).get()
            val iDevice = debugBridge?.devices?.find { it.serialNumber == device.id }

            if (iDevice != null) {
                val receiver = CollectingOutputReceiver()
                iDevice.executeShellCommand(command, receiver)
                receiver.getOutput()
            } else {
                LOG.warn("Device ${device.id} not found")
                ""
            }
        } catch (e: Exception) {
            LOG.error("Failed to execute command on device ${device.id}: $command", e)
            ""
        }
    }

    /**
     * Convert Android Studio's IDevice to our AdbDevice model
     */
    private fun convertToAdbDevice(device: IDevice): AdbDevice {
        val modelName = device.getProperty("ro.product.model") ?: "Unknown"
        val sdk = device.getProperty("ro.build.version.sdk") ?: "1"
        return AdbDevice(
            id = device.serialNumber,
            name = modelName,
            _sdk = sdk
        )
    }
}
