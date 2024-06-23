package commands

import models.AdbDevice
import models.AppPackage

class ListPackagesCommand(device: AdbDevice): AdbCommand<List<AppPackage>> {
    override val command: String = "-s ${device.id} shell pm list packages"

    override fun parse(result: CommandResult): List<AppPackage> {
        return result.content.split('\n')
            .map { AppPackage(it.removePrefix("package:")) }
            .sortedBy { it.packageName }
    }

}
