package services

import commands.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import device.AndroidStudioDeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.AdbDevice
import models.ActivityInfo
import models.AppPackage

/**
 * Command executor that bridges C3PO core commands with Android Studio's device management.
 * Adapts our business logic to work with Android Studio's ADB infrastructure.
 */
class PluginCommandExecutor(private val project: Project) {

    companion object {
        private val LOG = Logger.getInstance(PluginCommandExecutor::class.java)
    }

    private val deviceManager = AndroidStudioDeviceManager(project)

    /**
     * Get all connected devices
     */
    suspend fun getConnectedDevices(): List<AdbDevice> {
        return deviceManager.getConnectedDevices()
    }

    /**
     * List all activities for all packages on the specified device
     */
    suspend fun listActivities(device: AdbDevice): List<ActivityInfo> = withContext(Dispatchers.IO) {
        try {
            val command = ListActivitiesCommand()
            val output = deviceManager.executeCommand(device, command.command)

            // Use the command's parse method
            command.parse(output)
        } catch (e: Exception) {
            LOG.error("Failed to list activities on device ${device.id}", e)
            emptyList()
        }
    }

    /**
     * List all installed packages on the specified device
     */
    suspend fun listPackages(device: AdbDevice): List<AppPackage> = withContext(Dispatchers.IO) {
        try {
            val command = ListPackagesCommand()
            val output = deviceManager.executeCommand(device, command.command)

            // Use the command's parse method
            command.parse(output)
        } catch (e: Exception) {
            LOG.error("Failed to list packages on device ${device.id}", e)
            emptyList()
        }
    }

    /**
     * Start an activity on the specified device
     */
    suspend fun startActivity(device: AdbDevice, packageName: String, activityName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val activityInfo = ActivityInfo(packageName, activityName)
            val command = StartActivityCommand(activityInfo)
            val output = deviceManager.executeCommand(device, command.command)

            // Consider successful if no error in output
            !output.contains("Error") && !output.contains("Exception")
        } catch (e: Exception) {
            LOG.error("Failed to start activity $packageName/$activityName on device ${device.id}", e)
            false
        }
    }
}
