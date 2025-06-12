package commands

import com.android.ddmlib.IDevice
import com.android.tools.idea.adb.AdbService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import device.CollectingOutputReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import models.AdbDevice
import java.util.concurrent.TimeUnit

/**
 * Command execution strategy for Android Studio plugin using Android Studio's ADB integration.
 */
class AndroidStudioCommandExecutionStrategy(
    private val project: Project
) : CommandExecutionStrategy {

    companion object {
        private val LOG = Logger.getInstance(AndroidStudioCommandExecutionStrategy::class.java)
    }

    override suspend fun executeCommand(device: AdbDevice, commandSpec: CommandSpec): Result<String> = withContext(Dispatchers.IO) {
        try {
            LOG.info("Executing command on device ${device.id}: ${commandSpec.baseCommand}")
            val adbService = AdbService.getInstance()
            val debugBridge = adbService.getDebugBridge(project).get()
            val iDevice = debugBridge?.devices?.find { it.serialNumber == device.id }

            if (iDevice != null) {
                LOG.info("Device found, executing command...")
                val receiver = CollectingOutputReceiver()

                val finalCommand = buildCommand(commandSpec)
                LOG.info("Final command: $finalCommand")
                LOG.info("Using timeout: ${commandSpec.timeoutMs}ms")

                iDevice.executeShellCommand(finalCommand, receiver, commandSpec.timeoutMs, TimeUnit.MILLISECONDS)
                val output = receiver.getOutput()

                LOG.info("Command completed. Output length: ${output.length} characters")
                LOG.info("Output preview (first 500 chars): ${output.take(500)}")

                // Check for specific sections if it's a dumpsys command
                if (commandSpec.baseCommand.contains("dumpsys package")) {
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

                Result.success(output)
            } else {
                LOG.warn("Device ${device.id} not found in debug bridge")
                Result.failure(RuntimeException("Device ${device.id} not found"))
            }
        } catch (e: Exception) {
            LOG.error("Failed to execute command on device ${device.id}: ${commandSpec.baseCommand}", e)
            Result.failure(e)
        }
    }

    private fun buildCommand(commandSpec: CommandSpec): String {
        return when (commandSpec.executionType) {
            CommandExecutionType.ADB_DIRECT -> {
                // For Android Studio, ADB direct commands are not supported through executeShellCommand
                // This would need to be handled differently
                throw UnsupportedOperationException("ADB direct commands not supported in Android Studio strategy")
            }
            CommandExecutionType.SHELL_COMMAND,
            CommandExecutionType.PACKAGE_MANAGER,
            CommandExecutionType.ACTIVITY_MANAGER,
            CommandExecutionType.SYSTEM_DUMP -> {
                // For Android Studio, we just pass the base command since executeShellCommand handles the shell part
                commandSpec.baseCommand
            }
        }
    }
}
