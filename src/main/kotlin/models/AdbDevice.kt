package models

import javax.print.attribute.standard.PrinterMoreInfoManufacturer


data class AdbDevice(
    val id: String,
    val details: DeviceDetails = DeviceDetails()
)

data class DeviceDetails(
    val name: String? = null,
    val model: String? = null,
    val brand: String? = null,
    val manufacturer: String? = null,
    val sdkLevel: String? = null,
    val serialNumber: String? = null
)