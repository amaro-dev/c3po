package commands

import CommandRunner
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
        return if (result.resultCode != 0)
            Result.failure(Exception(result.error))
        else
            Result.success(command.parse(result))
    }

}
