package models


data class AdbDevice(
    val id: String,
    val details: DeviceDetails = DeviceDetails()
)

data class DeviceDetails(
    val name: String? = null,
    val model: String? = null,
    val brand: String? = null,
    val sdkLevel: String? = null,
    val serialNumber: String? = null
)