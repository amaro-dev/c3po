package commands

import core.AppState
import dev.amaro.sonic.IProcessor
import models.AdbDevice


class CommandExecutor {
    suspend fun <T> go(command: AdbCommand<T>, processor: IProcessor<AppState>, adbPath: String, device: AdbDevice?): T {
        return command.parse(
            CommandRunner.run(
                "$adbPath ${device?.id?.let { "-s $it " } ?: ""}${command.command.trim()}"
            )
        )
    }
}
