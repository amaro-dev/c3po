package commands

import models.AdbDevice

class ListDevicesCommand :
    AdbCommand<List<AdbDevice>>,
    PlaceholderAdb {
    override val command: String =
        "/bin/bash¡-c¡[ADB] devices | awk \"NR>1 && \\$2==\\\"device\\\" {print \\$1}\" | xargs -S1024 -n1 -I{} sh -c \"model=\\$([ADB] -s \\\"{}\\\" shell getprop ro.product.model | tr -d \\\"\\r\\\"); sdk=\\$([ADB] -s \\\"{}\\\" shell getprop ro.build.version.sdk | tr -d \\\"\\r\\\"); echo \\\"{}\\t\\\$model\\t\\\$sdk\\\"\""

    private val separator = Regex("\\t")

    override fun parse(result: String): List<AdbDevice> =
        result
            .split("\n")
            .filter { it.isNotEmpty() }
            .map {
                val information = separator.split(it)
                val (id, name, sdk) = information
                AdbDevice(id, name, sdk)
            }
}

interface PlaceholderAdb
