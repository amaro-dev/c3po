package core.command

import core.model.AppPackage

class StopAppCommand(
    appPackage: AppPackage,
) : AdbCommand<Unit> {
    override val command: String = "shell am force-stop ${appPackage.packageName}"

    override fun parse(result: String) = Unit
}
