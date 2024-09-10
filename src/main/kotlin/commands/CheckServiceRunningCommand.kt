package commands

class CheckServiceRunningCommand(serviceName: String) : AdbCommand<Boolean> {
    override val command: String = "shell dumpsys activity services $serviceName"

    override fun parse(result: CommandResult) = result.content.contains("app=ProcessRecord")
}
