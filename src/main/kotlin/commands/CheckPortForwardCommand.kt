package commands

class CheckPortForwardCommand(
    private val port: String,
) : AdbCommand<Boolean> {
    override val command: String = "forward --list"

    override fun parse(result: String) = result.trim().contains("tcp:$port")
}
