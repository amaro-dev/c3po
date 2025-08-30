package core.model

data class DeviceInfo(
    val device: Device,
    val system: System,
    val status: Status
)

data class Device(
    val model: String,
    val brand: String,
    val processor: String,
    val architecture: String,
    val serialNumber: String,
    val ramSize: String,
    val screenSize: String,
    val screenResolution: String
)

data class System(
    val androidVersion: String,
    val securityPatch: String,
    val build: String,
    val firmware: String
)

data class Status(
    val batteryLevel: Int,
    val batteryHealth: String,
    val batteryTemperature: Float,
    val chargingStatus: String,
    val batteryVoltage: Int,
    val diskUsage: List<DiskPartition>,
    val connectionMode: String,
    val connectionDetails: String?
)

data class DiskPartition(
    val partition: String,
    val used: String,
    val total: String,
    val percentage: Int,
    val mountPoint: String
)

data class BatteryInfo(
    val level: Int,
    val health: String,
    val temperature: Float,
    val chargingStatus: String,
    val voltage: Int
)

data class DisplayInfo(
    val size: String,
    val density: String
)