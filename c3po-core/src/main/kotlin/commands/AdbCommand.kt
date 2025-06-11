package commands

interface AdbCommand<T> {
    val command: String

    fun parse(result: String): T

    fun run(adbPath: String): T =
        parse("")
}
