package services

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import commands.AndroidStudioCommandExecutionStrategy
import core.command.ListActivitiesCommand
import core.command.ListPackagesCommand
import core.command.StartActivityCommand
import core.command.UnifiedCommandExecutor
import core.model.ActivityInfo
import core.model.AdbDevice
import core.model.AppPackage
import device.AndroidStudioDeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhanced command executor that uses the new unified command execution architecture.
 * This bridges C3PO core commands with Android Studio's device management using the strategy pattern.
 */
class PluginCommandExecutor(
    private val project: Project,
) {
    companion object {
        private val LOG = Logger.getInstance(PluginCommandExecutor::class.java)
    }

    private val deviceManager = AndroidStudioDeviceManager(project)
    private val executionStrategy = AndroidStudioCommandExecutionStrategy(project)
    private val commandExecutor = UnifiedCommandExecutor(executionStrategy)

    /**
     * Get all connected devices
     */
    suspend fun getConnectedDevices(): List<AdbDevice> = deviceManager.getConnectedDevices()

    /**
     * List all activities for all packages on the specified device using the new architecture
     */
    suspend fun listActivities(device: AdbDevice): List<ActivityInfo> =
        withContext(Dispatchers.IO) {
            try {
                val command = ListActivitiesCommand()
                val result = commandExecutor.execute(command, device)

                if (result.isSuccess) {
                    result.getOrThrow()
                } else {
                    LOG.error("Failed to list activities on device ${device.id}", result.exceptionOrNull())
                    emptyList()
                }
            } catch (e: Exception) {
                LOG.error("Failed to list activities on device ${device.id}", e)
                emptyList()
            }
        }

    /**
     * List all installed packages on the specified device using the new architecture
     */
    suspend fun listPackages(device: AdbDevice): List<AppPackage> =
        withContext(Dispatchers.IO) {
            try {
                val command = ListPackagesCommand()
                val result = commandExecutor.execute(command, device)

                if (result.isSuccess) {
                    result.getOrThrow()
                } else {
                    LOG.error("Failed to list packages on device ${device.id}", result.exceptionOrNull())
                    emptyList()
                }
            } catch (e: Exception) {
                LOG.error("Failed to list packages on device ${device.id}", e)
                emptyList()
            }
        }

    /**
     * Start an activity on the specified device
     */
    suspend fun startActivity(
        device: AdbDevice,
        packageName: String,
        activityName: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val activityInfo = ActivityInfo(packageName, activityName)
                val command = StartActivityCommand(activityInfo)
                val result = commandExecutor.execute(command, device)

                if (result.isSuccess) {
                    // For StartActivityCommand, we need to check if the result indicates success
                    // This depends on how StartActivityCommand.parse() works
                    true
                } else {
                    LOG.error(
                        "Failed to start activity $packageName/$activityName on device ${device.id}",
                        result.exceptionOrNull()
                    )
                    false
                }
            } catch (e: Exception) {
                LOG.error("Failed to start activity $packageName/$activityName on device ${device.id}", e)
                false
            }
        }
}
