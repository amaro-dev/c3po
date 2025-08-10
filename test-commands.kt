import commands.*
import kotlinx.coroutines.runBlocking

fun main() {
    println("C3PO Commands Test")
    println("==================")

    runBlocking {
        val commandExecutor = CommandExecutor()
        val adbPath = "/Users/roarodrigues/Library/Android/sdk/platform-tools/adb"

        try {
            // Test device listing
            println("\n1. Testing device detection...")
            val listDevicesCommand = ListDevicesCommand()
            val devicesResult = commandExecutor.go(listDevicesCommand, adbPath)

            if (devicesResult.isSuccess) {
                val devices = devicesResult.getOrNull()!!
                println("Found ${devices.size} device(s):")
                devices.forEach { device ->
                    println("  - ${device.name} (${device.id})")
                }

                if (devices.isNotEmpty()) {
                    val firstDevice = devices.first()
                    println("\n2. Testing activity listing on device: ${firstDevice.name}")

                    // Test activity listing
                    val listActivitiesCommand = ListActivitiesCommand()
                    val activitiesResult = commandExecutor.go(listActivitiesCommand, adbPath, firstDevice)

                    if (activitiesResult.isSuccess) {
                        val activities = activitiesResult.getOrNull()!!
                        println("Found ${activities.size} activities:")
                        activities.take(5).forEach { activity ->
                            println("  - ${activity.packageName}/${activity.activityPath}")
                        }
                        if (activities.size > 5) {
                            println("  ... and ${activities.size - 5} more")
                        }
                    } else {
                        println("Failed to list activities: ${activitiesResult.exceptionOrNull()?.message}")
                        activitiesResult.exceptionOrNull()?.printStackTrace()
                    }
                }
            } else {
                println("Failed to list devices: ${devicesResult.exceptionOrNull()?.message}")
                devicesResult.exceptionOrNull()?.printStackTrace()
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
            e.printStackTrace()
        }
    }

    println("\nTest completed!")
}
