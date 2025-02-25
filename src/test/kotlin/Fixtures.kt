import commands.AdbCommand
import commands.PlaceholderAdb


class FakeCommand(
    override val command: String,
) : AdbCommand<String> {
    override fun parse(result: String): String = result
}

class FakePlaceholderCommand(
    override val command: String,
) : AdbCommand<String>, PlaceholderAdb {
    override fun parse(result: String): String = result
}
