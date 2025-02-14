package commands

import models.AdbDevice
import java.io.File

class CommandExecutor {
    suspend fun <T> go(
        command: AdbCommand<T>,
        adbPath: String,
        device: AdbDevice? = null,
    ): Result<T> {
        val finalCommand = if (command is PlaceholderAdb) {
            command.command
                .trim()
                .replace(Regex("\\[ADB\\]"), "${adbPath.trim()} ${device?.id?.let { "-s $it " } ?: ""}")
                .split("¡")
        } else {
            "$adbPath ${device?.id?.let { "-s $it " } ?: ""}${command.command.trim()}".split(" ")
        }
        val result = CommandRunner.run(File(adbPath.substringBeforeLast("/")), finalCommand.toTypedArray())
        return if (result.isFailure)
            Result.failure(result.exceptionOrNull() ?: UnknownError())
        else
            Result.success(command.parse(result.getOrNull()!!))
    }

}
