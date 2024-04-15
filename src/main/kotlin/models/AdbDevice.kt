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

    Board("ro.product.board"),
    SerialNumber("ro.serialno"),

    AndroidVersion("ro.build.version.release"),
    AndroidSdk("ro.build.version.sdk"),
    Build("ro.build.id"),

}