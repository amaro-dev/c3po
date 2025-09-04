package core.command

import core.logging.StructuredLogger
import core.model.AdbDevice
import java.io.File
import kotlin.system.measureTimeMillis

class CommandExecutor {
    private val logger = StructuredLogger.getInstance()

    suspend fun <T> go(
        command: AdbCommand<T>,
        adbPath: String,
        device: AdbDevice? = null,
    ): Result<T> {
        val commandName = command::class.simpleName ?: "UnknownCommand"
        val deviceInfo = device?.let { "${it.name} (${it.id})" } ?: "no device"
        
        // Log command execution start
        logger.log("adb", commandName, "Starting ADB command: ${command.command} on $deviceInfo")
        
        val finalCommand = CommandBuilder.build(command, adbPath, device)
        
        var result: Result<String>
        val executionTimeMs = measureTimeMillis {
            result = try {
                CommandRunner.run(File(adbPath.substringBeforeLast("/")), finalCommand)
            } catch (e: Exception) {
                logger.log("adb", commandName, "ADB command failed with exception: ${e.message}")
                throw e
            }
        }
        
        return if (result.isFailure) {
            val error = result.exceptionOrNull()?.message ?: "Unknown error"
            logger.log("adb", commandName, "ADB command failed after ${executionTimeMs}ms: $error")
            Result.failure(result.exceptionOrNull() ?: UnknownError())
        } else {
            logger.log("adb", commandName, "ADB command completed successfully in ${executionTimeMs}ms")
            Result.success(command.parse(result.getOrNull()!!))
        }
    }
}
