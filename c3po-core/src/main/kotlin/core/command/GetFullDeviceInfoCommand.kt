package core.command

import core.model.Device
import core.model.DeviceInfo
import core.model.Status
import core.model.System

class GetFullDeviceInfoCommand : AdbCommand<DeviceInfo> {
    override val command: String = "shell getprop"

    override fun parse(result: String): DeviceInfo {
        val props = parseProperties(result)
        
        return DeviceInfo(
            device = parseDevice(props),
            system = parseSystem(props),
            status = parseStatusFromProps(props)
        )
    }

    private fun parseProperties(result: String): Map<String, String> {
        return result.lines()
            .filter { it.contains("]: [") && it.startsWith("[") && it.endsWith("]") }
            .associate { line ->
                val keyEnd = line.indexOf("]: [")
                if (keyEnd > 0) {
                    val key = line.substring(1, keyEnd)
                    val value = line.substring(keyEnd + 4, line.length - 1)
                    key to value
                } else {
                    "" to ""
                }
            }
            .filterKeys { it.isNotEmpty() }
    }

    private fun parseDevice(props: Map<String, String>): Device {
        // Extract RAM info from available props
        val ramSize = extractRamInfo(props)

        // Extract screen info from props  
        val (screenSize, screenDensity) = extractScreenInfo(props)

        return Device(
            model = props["ro.product.model"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            brand = props["ro.product.brand"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            processor = props["ro.hardware"]?.takeIf { it.isNotEmpty() }
                ?: props["ro.board"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            architecture = props["ro.product.cpu.abi"]?.takeIf { it.isNotEmpty() }
                ?: props["ro.arch"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            serialNumber = props["ro.serialno"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            ramSize = ramSize,
            screenSize = screenSize,
            screenResolution = screenDensity
        )
    }

    private fun parseSystem(props: Map<String, String>): System {
        return System(
            androidVersion = props["ro.build.version.release"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            securityPatch = props["ro.build.version.security_patch"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            build = props["ro.build.display.id"]?.takeIf { it.isNotEmpty() } ?: "Unknown",
            firmware = props["ro.build.fingerprint"]?.takeIf { it.isNotEmpty() } ?: "Unknown"
        )
    }

    private fun parseStatusFromProps(props: Map<String, String>): Status {
        // Basic connection info from props
        val connectionMode = when {
            props["wifi.interface"] != null -> "Wi-Fi"
            props["ro.telephony.default_network"] != null -> "Mobile"
            else -> "Unknown"
        }

        return Status(
            batteryLevel = 0, // Requires separate dumpsys battery command
            batteryHealth = "Unknown", // Requires separate dumpsys battery command
            batteryTemperature = 0f, // Requires separate dumpsys battery command
            chargingStatus = "Unknown", // Requires separate dumpsys battery command
            batteryVoltage = 0, // Requires separate dumpsys battery command
            diskUsage = emptyList(), // Requires separate df command
            connectionMode = connectionMode,
            connectionDetails = props["wifi.interface"]
        )
    }

    private fun extractRamInfo(props: Map<String, String>): String {
        // Try to get RAM info from various properties
        return props["dalvik.vm.heapmaxfree"] // Not total RAM but related
            ?: props["ro.config.low_ram"]?.let { if (it == "true") "Low RAM device" else "Standard RAM" }
            ?: "Unknown"
    }

    private fun extractScreenInfo(props: Map<String, String>): Pair<String, String> {
        val density = props["ro.sf.lcd_density"] ?: "Unknown"

        // Try to extract screen size from props
        val screenSize = props["ro.sf.lcd_width"]?.let { width ->
            props["ro.sf.lcd_height"]?.let { height ->
                "${width}x${height}"
            }
        } ?: "Unknown"

        return screenSize to "$density DPI"
    }
}