package core.command

class CheckServiceRunningCommand(
    serviceName: String,
) : AdbCommand<Boolean> {
    override val command: String = "shell dumpsys activity services $serviceName"

    override fun parse(result: String) = result.contains("app=ProcessRecord")
}
