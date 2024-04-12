package models


data class AdbDevice(
    val id: String,
    val details: Map<String, String> = emptyMap()
)

enum class DeviceAttrs(val key: String) {
    Name("ro.product.name"),
    Model("ro.product.model"),
    Brand("ro.product.brand"),
    Manufacturer("ro.product.manufacturer"),
    AndroidSdk("ro.build.version.sdk"),
    SerialNumber("ro.serialno")
}