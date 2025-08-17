package core.command

class ForwardPortCommand(
    port: String,
) : AdbCommand<Unit> {
    override val command: String = "forward tcp:$port tcp:$port"

    override fun parse(result: String) = Unit
}
