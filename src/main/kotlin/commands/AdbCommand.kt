package commands

import CommandRunner

interface AdbCommand<T> {
    val command: String

    fun parse(result: CommandResult) : T

    fun run(): T {
        return parse(CommandRunner.run(command.trim()))
    }

}