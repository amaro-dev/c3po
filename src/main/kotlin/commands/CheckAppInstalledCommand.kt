package commands

class CheckAppInstalledCommand(packageName: String) : AdbCommand<Boolean> {
    override val command: String = "shell pm list packages | grep $packageName"

    override fun parse(result: CommandResult) = result.content.trim().isNotEmpty()
}
