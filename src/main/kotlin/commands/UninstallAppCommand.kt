package commands

import models.AppPackage

class UninstallAppCommand(
    appPackage: AppPackage,
) : AdbCommand<Unit> {
    override val command: String = "uninstall ${appPackage.packageName}"

    override fun parse(result: String) = Unit
}
