package core.command

import core.model.AdbDevice
import java.io.File

class CommandExecutor {
    suspend fun <T> go(
        command: AdbCommand<T>,
        adbPath: String,
        device: AdbDevice? = null,
    ): Result<T> {
        val finalCommand = CommandBuilder.build(command, adbPath, device)
        val result = CommandRunner.run(File(adbPath.substringBeforeLast("/")), finalCommand)
        return if (result.isFailure) {
            Result.failure(result.exceptionOrNull() ?: UnknownError())
        } else {
            Result.success(command.parse(result.getOrNull()!!))
        }
    }
}
