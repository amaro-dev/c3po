package core.command

import core.model.BatteryInfo

class GetBatteryInfoCommand : AdbCommand<BatteryInfo> {
    override val command: String = "shell dumpsys battery"

    override fun parse(result: String): BatteryInfo {
        val lines = result.lines()

        val level = lines.find { it.trim().startsWith("level:") }
            ?.substringAfter("level:")?.trim()?.toIntOrNull() ?: 0

        val health = lines.find { it.trim().startsWith("health:") }
            ?.substringAfter("health:")?.trim()?.let {
                when (it) {
                    "2" -> "Good"
                    "3" -> "Overheat"
                    "4" -> "Dead"
                    "5" -> "Over voltage"
                    "6" -> "Unknown failure"
                    "7" -> "Cold"
                    else -> "Unknown"
                }
            } ?: "Unknown"

        val temperature = lines.find { it.trim().startsWith("temperature:") }
            ?.substringAfter("temperature:")?.trim()?.toFloatOrNull()?.let { it / 10 } ?: 0f

        val chargingStatus = lines.find { it.trim().startsWith("status:") }
            ?.substringAfter("status:")?.trim()?.let {
                when (it) {
                    "1" -> "Unknown"
                    "2" -> "Charging"
                    "3" -> "Discharging"
                    "4" -> "Not charging"
                    "5" -> "Full"
                    else -> "Unknown"
                }
            } ?: "Unknown"

        val voltage = lines.find { it.trim().startsWith("voltage:") }
            ?.substringAfter("voltage:")?.trim()?.toIntOrNull() ?: 0

        return BatteryInfo(level, health, temperature, chargingStatus, voltage)
    }
}