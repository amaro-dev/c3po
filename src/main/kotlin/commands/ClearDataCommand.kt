package commands

import models.AppPackage

class ClearDataCommand(
    appPackage: AppPackage,
) : AdbCommand<Unit> {
    override val command: String = "shell pm clear ${appPackage.packageName}"

    override fun parse(result: CommandResult) = Unit
}
