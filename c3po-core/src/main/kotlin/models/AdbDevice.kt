package models

data class AdbDevice(
    val id: String,
    val name: String,
    private val _sdk: String = "1"
) {
    val sdk: Int = _sdk.toInt()
}
