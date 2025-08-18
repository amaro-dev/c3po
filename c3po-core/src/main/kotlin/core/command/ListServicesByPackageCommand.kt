package core.command

import core.model.ActivityInfo

class ListServicesByPackageCommand : EnhancedAdbCommand<List<Pair<String, List<ActivityInfo>>>> {
    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "dumpsys package",
            executionType = CommandExecutionType.SYSTEM_DUMP,
            timeoutMs = 30000L, // 30 seconds for dumpsys commands
            requiresShell = true,
        )

    override fun parse(result: String): List<Pair<String, List<ActivityInfo>>> {
        val servicesMap = mutableMapOf<String, MutableList<ActivityInfo>>()

        // Find the "Service Resolver Table" section
        val serviceTableIndex = result.indexOf("Service Resolver Table")
        if (serviceTableIndex == -1) {
            return emptyList()
        }

        // Find the end of the Service Resolver Table 
        // Look for the start of the next major section
        val serviceSection = result.substring(serviceTableIndex)

        // The service resolver table ends when we hit another resolver table or major section
        val endPatterns = listOf(
            "Receiver Resolver Table",
            "ContentProvider Resolver Table",
            "KeySet Manager:",
            "Queries:",
            "Pending verification:",
            "Package Changes:"
        )

        var nextSectionIndex = serviceSection.length
        for (pattern in endPatterns) {
            val index = serviceSection.indexOf(pattern)
            if (index != -1 && index < nextSectionIndex) {
                nextSectionIndex = index
            }
        }

        val serviceTableSection = serviceSection.substring(0, nextSectionIndex)
        val lines = serviceTableSection.lines()

        for (line in lines) {
            // Look for service entries like: "        115144f com.google.android.gms/.car.InCallService2"
            // Pattern: spaces + hex hash + package/service (without additional text)
            val serviceMatch = Regex("""^\s+[a-f0-9]+\s+([^/\s]+)/(\.[^\s]+)$""").find(line)
            if (serviceMatch != null) {
                val packageName = serviceMatch.groupValues[1]
                val servicePath = serviceMatch.groupValues[2]

                val activityInfo = ActivityInfo(
                    packageName = packageName,
                    activityPath = servicePath
                )

                servicesMap.getOrPut(packageName) { mutableListOf() }.add(activityInfo)
            }
        }

        // Convert to the expected format: List<Pair<String, List<ActivityInfo>>>
        return servicesMap.map { (packageName, services) ->
            Pair(packageName, services.sortedBy { it.activityPath })
        }.sortedBy { it.first }
    }
}