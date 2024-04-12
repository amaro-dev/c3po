package commands

import models.AdbDevice
import models.DeviceDetails

class DeviceInfoCommand(device: AdbDevice): AdbCommand<DeviceDetails> {
    override val command: String ="adb -s ${device.id} shell getprop | grep ro."

    override fun parse(result: CommandResult): DeviceDetails {
        val lineSplitRule = Regex("\\r?\\n")
        val map = lineSplitRule.split(result.content)
            .map { it.trim().subSequence(1, it.length-1) }
            .map { it.split("]: [") }
            .filter { it.size == 2 }
            .associate { Pair(it[0], it[1]) }
        return DeviceDetails(
            map["ro.product.name"],
            map["ro.product.model"],
            map["ro.product.brand"],
            map["ro.product.manufacturer"],
            map["ro.build.version.sdk"],
            map["ro.serialno"]
        )
    }
}