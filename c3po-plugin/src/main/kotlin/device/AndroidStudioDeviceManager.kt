package device

import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import core.model.AdbDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

/**
 * Device manager that integrates with Android Studio's built-in device management.
 * Uses Android Studio's ADB infrastructure instead of maintaining our own.
 */
class AndroidStudioDeviceManager(
    private val project: Project,
) {
    companion object {
        private val LOG = Logger.getInstance(AndroidStudioDeviceManager::class.java)
    }

    /**
     * Get connected devices using Android Studio's device management
     */
    suspend fun getConnectedDevices(): List<AdbDevice> =
        withContext(Dispatchers.IO) {
            try {
                LOG.info("Attempting to get connected devices...")
                val adbService = AdbService.getInstance()
                LOG.info("AdbService instance obtained")

                val debugBridgeFuture = adbService.getDebugBridge(project)
                LOG.info("Debug bridge future obtained")

                val debugBridge =
                    withTimeoutOrNull(5000) {
                        debugBridgeFuture.get(5, TimeUnit.SECONDS)
                    }
                LOG.info("Debug bridge: $debugBridge")

                if (debugBridge == null) {
                    LOG.warn("Debug bridge is null - ADB might not be initialized")
                    return@withContext emptyList()
                }

                // Get all devices (including offline ones)
                val allDevices = debugBridge.devices
                LOG.info("Raw devices array: ${allDevices?.contentToString()}")
                LOG.info("Raw devices count: ${allDevices?.size ?: 0}")

                if (allDevices != null) {
                    for (device in allDevices) {
                        LOG.info(
                            "Device details: serial=${device.serialNumber}, state=${device.state}, name=${device.name}, isOnline=${device.isOnline}",
                        )
                    }
                }

                // Filter for online devices only
                val onlineDevices =
                    allDevices
                        ?.filter { device ->
                            val isOnline = device.isOnline
                            LOG.info("Device ${device.serialNumber} online status: $isOnline")
                            isOnline
                        }?.map { device ->
                            LOG.info("Converting device: ${device.serialNumber} - ${device.name}")
                            convertToAdbDevice(device)
                        } ?: emptyList()

                LOG.info("Found ${onlineDevices.size} online devices: ${onlineDevices.map { "${it.name} (${it.id})" }}")
                onlineDevices
            } catch (e: Exception) {
                LOG.error("Failed to get connected devices", e)
                emptyList()
            }
        }

    /**
     * Execute shell command on device using Android Studio's ADB integration
     */
    suspend fun executeCommand(
        device: AdbDevice,
        command: String,
    ): String =
        withContext(Dispatchers.IO) {
            try {
                LOG.info("Executing command on device ${device.id}: $command")
                val adbService = AdbService.getInstance()
                val debugBridge = adbService.getDebugBridge(project).get()
                val iDevice = debugBridge?.devices?.find { it.serialNumber == device.id }

                if (iDevice != null) {
                    LOG.info("Device found, executing command...")
                    val receiver = CollectingOutputReceiver()

                    // Execute with a longer timeout for dumpsys commands
                    val timeout = if (command.contains("dumpsys")) 30000L else 10000L
                    LOG.info("Using timeout: ${timeout}ms")

                    iDevice.executeShellCommand(command, receiver, timeout, TimeUnit.MILLISECONDS)
                    val output = receiver.getOutput()

                    LOG.info("Command completed. Output length: ${output.length} characters")
                    LOG.info("Output preview (first 500 chars): ${output.take(500)}")

                    // Check for specific sections
                    if (command.contains("dumpsys package")) {
                        val hasActivityResolver = output.contains("Activity Resolver Table")
                        val hasPackages = output.contains("Packages:")
                        LOG.info("Activity Resolver Table found: $hasActivityResolver")
                        LOG.info("Packages section found: $hasPackages")

                        if (!hasActivityResolver) {
                            LOG.warn("Activity Resolver Table not found in output")
                        }
                        if (!hasPackages) {
                            LOG.warn("Packages section not found in output")
                        }
                    }

                    output
                } else {
                    LOG.warn("Device ${device.id} not found in debug bridge")
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
            _sdk = sdk,
        )
    }
}
