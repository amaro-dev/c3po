package commands

import CommandRunner

interface AdbCommand<T> {
    val command: String

    fun parse(result: CommandResult) : T

    fun run(adbPath: String): T {
        return parse(
            CommandRunner.run(
                "$adbPath ${command.trim()}"
            )
        )
    }
}
