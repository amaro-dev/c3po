package models


data class AdbDevice(
    val id: String,
    val details: Map<String, String> = emptyMap()
)
