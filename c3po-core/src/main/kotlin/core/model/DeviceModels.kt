package core.model

data class DeviceInfo(
    val identity: DeviceIdentity,
    val hardware: HardwareInfo,
    val performance: PerformanceInfo,
    val network: NetworkInfo,
    val build: BuildInfo,
    val features: HardwareFeatures
)

data class DeviceIdentity(
    val manufacturer: String,
    val model: String,
    val product: String,
    val brand: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val serialNumber: String,
    val fingerprint: String
)

data class HardwareInfo(
    val cpuArchitecture: String,
    val cpuCores: Int,
    val cpuFeatures: String,
    val totalRam: Long,
    val availableRam: Long,
    val displayWidth: Int,
    val displayHeight: Int,
    val displayDensity: Int,
    val totalStorage: Long,
    val usedStorage: Long
)

data class PerformanceInfo(
    val uptime: Long,
    val processCount: Int,
    val cpuUsage: Float,
    val memoryUsage: Float,
    val totalApps: Int,
    val userApps: Int,
    val systemApps: Int,
    val batteryLevel: Int,
    val batteryStatus: String,
    val powerSource: String
)

data class NetworkInfo(
    val wifiConnected: Boolean,
    val wifiSsid: String?,
    val ipAddress: String?,
    val mobileCarrier: String?,
    val mobileNetworkType: String?,
    val simState: String?,
    val operatorName: String?
)

data class BuildInfo(
    val buildId: String,
    val buildType: String,
    val buildDate: String,
    val buildHost: String,
    val buildUser: String,
    val buildTags: String
)

data class HardwareFeatures(
    val hasNfc: Boolean,
    val hasBluetooth: Boolean,
    val hasCamera: Boolean,
    val hasGps: Boolean,
    val hasSensors: Boolean,
    val hasFingerprint: Boolean,
    val hasWifi: Boolean,
    val hasUsb: Boolean,
    val hasTelephony: Boolean,
    val hasSdCard: Boolean
)