package core.command

import core.debug
import core.model.AdbDevice
import core.model.DeviceNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Command execution strategy for desktop applications using ProcessBuilder and external ADB binary.
 */
class DesktopCommandExecutionStrategy(
    private val adbPath: String,
) : CommandExecutionStrategy {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun executeCommand(
        device: AdbDevice,
        commandSpec: CommandSpec,
    ): Result<String> {
        val fullCommand = buildCommand(device, commandSpec)
        debug("Desktop Command: '${fullCommand.joinToString(" ")}'")

        return withContext(Dispatchers.IO) {
            try {
                debug("Will start command")
                val process =
                    ProcessBuilder()
                        .command(*fullCommand)
                        .start()
                debug("Command started")

                val response = async { process.inputReader().readText().trim() }
                val error = async { process.errorReader().readText().trim() }
                val exited = process.waitFor(commandSpec.timeoutMs, TimeUnit.MILLISECONDS)

                if (!exited) {
                    process.destroyForcibly()
                    response.cancel()
                    error.cancel()
                    debug("Command timed out!")
                    Result.failure(TimeoutException("Command timed out after ${commandSpec.timeoutMs}ms"))
                } else {
                    val responseText = response.await()
                    val errorText = error.await()

                    if (process.exitValue() != 0) {
                        debug("Command failed with exit code ${process.exitValue()}: $errorText")
                        if (errorText.contains("device not found") || errorText.contains("no devices")) {
                            Result.failure(DeviceNotFoundException())
                        } else {
                            Result.failure(RuntimeException("Command failed: $errorText"))
                        }
                    } else {
                        debug("Command succeeded. Output length: ${responseText.length}")
                        Result.success(responseText)
                    }
                }
            } catch (e: Exception) {
                debug("Command execution failed: ${e.message}")
                Result.failure(e)
            }
        }
    }

    private fun buildCommand(
        device: AdbDevice,
        commandSpec: CommandSpec,
    ): Array<String> {
        val deviceDirective = "-s ${device.id}"

        return when (commandSpec.executionType) {
            CommandExecutionType.ADB_DIRECT -> {
                // Direct ADB commands: adb -s device_id command
                arrayOf(adbPath, deviceDirective, *commandSpec.baseCommand.split(" ").toTypedArray())
            }

            CommandExecutionType.SHELL_COMMAND,
            CommandExecutionType.PACKAGE_MANAGER,
            CommandExecutionType.ACTIVITY_MANAGER,
            CommandExecutionType.SYSTEM_DUMP,
                -> {
                // Shell commands: adb -s device_id shell command
                arrayOf(adbPath, deviceDirective, "shell", *commandSpec.baseCommand.split(" ").toTypedArray())
            }
        }
    }
}
