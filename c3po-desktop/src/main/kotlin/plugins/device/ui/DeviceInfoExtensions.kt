package plugins.device.ui

import core.model.DeviceInfo
import core.model.HardwareFeatures
import kotlin.math.pow

fun DeviceInfo.toDisplayItems(): List<List<DeviceInfoItem>> {
    return listOf(
        // Top row - 3 cards
        listOf(
            // Device Identity
            listOf(
                DeviceInfoItem("📱", "Manufacturer", "${identity.manufacturer} ${identity.model}"),
                DeviceInfoItem("🤖", "Android", "${identity.androidVersion} (API ${identity.apiLevel})"),
                DeviceInfoItem("🔒", "Security Patch", identity.securityPatch),
                DeviceInfoItem("🏷️", "Serial", identity.serialNumber)
            ),
            // Hardware & Display
            listOf(
                DeviceInfoItem("🧠", "CPU", "${hardware.cpuArchitecture} (${hardware.cpuCores} cores)"),
                DeviceInfoItem(
                    "💾",
                    "RAM",
                    "${formatBytes(hardware.totalRam)} (${formatBytes(hardware.availableRam)} free)"
                ),
                DeviceInfoItem(
                    "🖥️",
                    "Display",
                    "${hardware.displayWidth}x${hardware.displayHeight}, ${hardware.displayDensity} DPI"
                ),
                DeviceInfoItem(
                    "💿",
                    "Storage",
                    "${formatBytes(hardware.totalStorage)} (${((hardware.usedStorage.toDouble() / hardware.totalStorage) * 100).toInt()}% used)"
                )
            ),
            // Status
            listOf(
                DeviceInfoItem("🔋", "Battery", "${performance.powerSource}, ${performance.batteryLevel}%"),
                DeviceInfoItem("📶", "Network", if (network.wifiConnected) "WiFi Connected" else "Disconnected"),
                DeviceInfoItem(
                    "📱",
                    "Mobile",
                    "${network.mobileCarrier ?: "N/A"} ${network.mobileNetworkType ?: ""}".trim()
                ),
                DeviceInfoItem("⚡", "Uptime", formatUptime(performance.uptime))
            )
        ),

        // Middle row - 2 cards
        listOf(
            // Build Information
            listOf(
                DeviceInfoItem("🔧", "Build", "${build.buildId} (${build.buildType})"),
                DeviceInfoItem("📅", "Built", build.buildDate),
                DeviceInfoItem("🏠", "Host", build.buildHost),
                DeviceInfoItem("👤", "Builder", build.buildUser)
            ),
            // System Performance
            listOf(
                DeviceInfoItem("🔄", "Processes", "${performance.processCount} running"),
                DeviceInfoItem(
                    "📊",
                    "Usage",
                    "CPU: ${performance.cpuUsage}% • RAM: ${performance.memoryUsage.toInt()}%"
                ),
                DeviceInfoItem("💽", "Partitions", "Data: ${formatBytes(hardware.usedStorage)}"),
                DeviceInfoItem(
                    "📦",
                    "Apps",
                    "${performance.totalApps} (${performance.userApps} user, ${performance.systemApps} system)"
                )
            )
        ),

        // Bottom row - 2 cards
        listOf(
            // Network Details
            listOf(
                DeviceInfoItem("🌐", "Connection", network.ipAddress ?: "Not Available"),
                DeviceInfoItem("📱", "Carrier", "${network.mobileCarrier ?: "N/A"} (${network.operatorName ?: "N/A"})"),
                DeviceInfoItem(
                    "📡",
                    "Network",
                    "${network.mobileNetworkType ?: "N/A"} • SIM: ${network.simState ?: "N/A"}"
                ),
                DeviceInfoItem("🔗", "Interface", if (network.wifiConnected) "wlan0" else "Not Connected")
            ),
            // Hardware Features
            listOf(
                DeviceInfoItem("✅", "Available", buildFeaturesList(features, true), false),
                DeviceInfoItem("❌", "Missing", buildFeaturesList(features, false), false)
            )
        )
    ).flatten()
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

private fun formatUptime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return "${hours}h ${minutes}m"
}

private fun buildFeaturesList(features: HardwareFeatures, available: Boolean): String {
    val featureMap = mapOf(
        "NFC" to features.hasNfc,
        "Bluetooth" to features.hasBluetooth,
        "Camera" to features.hasCamera,
        "GPS" to features.hasGps,
        "Sensors" to features.hasSensors,
        "Fingerprint" to features.hasFingerprint,
        "WiFi" to features.hasWifi,
        "USB" to features.hasUsb,
        "Telephony" to features.hasTelephony,
        "SD Card" to features.hasSdCard
    )

    return featureMap.filter { it.value == available }.keys.joinToString(" • ")
}