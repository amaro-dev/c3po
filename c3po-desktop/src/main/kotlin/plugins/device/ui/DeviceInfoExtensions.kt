package plugins.device.ui

import core.model.DeviceInfo
import core.model.DiskPartition

fun DeviceInfo.toDisplayItems(): List<List<DeviceInfoItem>> {
    return listOf(
        // Device Card
        listOf(
            DeviceInfoItem("📱", "Model", device.model),
            DeviceInfoItem("🏭", "Brand", device.brand),
            DeviceInfoItem("🧠", "Processor", device.processor.trim()),
            DeviceInfoItem("⚙️", "Architecture", device.architecture.trim()),
            DeviceInfoItem("🏷️", "Serial", device.serialNumber),
            DeviceInfoItem("💾", "RAM", device.ramSize),
            DeviceInfoItem("📐", "Screen Size", device.screenSize),
            DeviceInfoItem("🔍", "Resolution", "${device.screenResolution} DPI")
        ),

        // System Card
        listOf(
            DeviceInfoItem("🤖", "Android", system.androidVersion),
            DeviceInfoItem("🔒", "Security Patch", system.securityPatch),
            DeviceInfoItem("🔨", "Build", system.build),
            DeviceInfoItem("📡", "Firmware", system.firmware)
        ),

        // Status Card (without disk usage)
        listOf(
            DeviceInfoItem("🔋", "Battery", "${status.batteryLevel}% (${status.batteryHealth})"),
            DeviceInfoItem("🌡️", "Temperature", "${status.batteryTemperature}°C"),
            DeviceInfoItem("⚡", "Charging", status.chargingStatus),
            DeviceInfoItem("⚡", "Voltage", "${status.batteryVoltage}mV"),
            DeviceInfoItem("🌐", "Connection", formatConnection(status.connectionMode, status.connectionDetails))
        )
    )
}

fun formatDiskUsage(diskPartitions: List<DiskPartition>): String {
    if (diskPartitions.isEmpty()) return "No data available"

    return diskPartitions.joinToString(" • ") { partition ->
        "${partition.mountPoint}: ${partition.percentage}% (${partition.used}/${partition.total})"
    }
}

fun formatConnection(mode: String, details: String?): String {
    return if (details != null) {
        "$mode ($details)"
    } else {
        mode
    }
}

