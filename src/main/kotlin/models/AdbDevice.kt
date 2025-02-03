package models

data class AdbDevice(
    val id: String,
    val details: Map<String, String> = emptyMap(),
) {
    val sdk: Int = details["ro.build.version.sdk"]?.toInt() ?: 1
}
