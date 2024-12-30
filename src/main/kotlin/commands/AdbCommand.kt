package commands

interface AdbCommand<T> {
    val command: String

    fun parse(result: CommandResult): T

    fun run(adbPath: String): T =
        parse(
            CommandResult("", 0, null),
        )
}
