package commands

import models.AdbDevice

class DeviceInfoCommand(device: AdbDevice) : AdbCommand<Map<String, String>> {
    override val command: String = "-s ${device.id} shell getprop"

    override fun parse(result: CommandResult): Map<String, String> {
        val lineSplitRule = Regex("\\r?\\n")
        return lineSplitRule.split(result.content)
            .map { it.trim().subSequence(1, it.length-1) }
            .map { it.split("]: [") }
            .filter { it.size == 2 }
            .associate { Pair(it[0], it[1]) }

    }
}
