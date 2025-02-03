package commands

import models.AdbDevice

class CommandExecutor {
    suspend fun <T> go(
        command: AdbCommand<T>,
        adbPath: String,
        device: AdbDevice?,
    ): Result<T> {
        val result = CommandRunner.run(
            "$adbPath ${device?.id?.let { "-s $it " } ?: ""}${command.command.trim()}",
        )
        return if (result.isFailure)
            Result.failure(result.exceptionOrNull() ?: UnknownError())
        else
            Result.success(command.parse(result.getOrNull()!!))
    }

}
