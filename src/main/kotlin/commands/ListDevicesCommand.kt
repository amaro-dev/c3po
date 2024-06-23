package commands

import models.AdbDevice

object ListDevicesCommand : AdbCommand<List<AdbDevice>> {
    override val command: String = "devices"

    override fun parse(result: CommandResult): List<AdbDevice> {
        return result.content.removePrefix("List of devices attached")
            .trim()
            .split("\n")
            .filter { it.isNotEmpty() }
            .map {
                val (id, name) = it.split('\t', limit = 2)
                AdbDevice(id)
            }
    }

}
