package core.command

import core.model.ActivityInfo

class ListActivitiesCommand : EnhancedAdbCommand<List<ActivityInfo>> {
    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "dumpsys package",
            executionType = CommandExecutionType.SYSTEM_DUMP,
            timeoutMs = 30000L, // 30 seconds for dumpsys commands
            requiresShell = true,
        )


    override fun parse(result: String): List<ActivityInfo> {
        val lineSplitRule = Regex("\\r?\\n")
        var content = result.trim()

        val activityResolverIndex = content.indexOf("Activity Resolver Table")
        if (activityResolverIndex == -1) {
            return emptyList()
        }

        content = content.substring(activityResolverIndex)

        val nonDataActionsIndex = content.indexOf("Non-Data Actions:")
        if (nonDataActionsIndex == -1) {
            return emptyList()
        }

        content = content.substring(nonDataActionsIndex)

        val mainActionIndex = content.indexOf("android.intent.action.MAIN:")
        if (mainActionIndex == -1) {
            return emptyList()
        }

        content = content.substring(mainActionIndex)
        val lines = lineSplitRule.split(content)

        val activities =
            lines
                .drop(1) // Skip the "android.intent.action.MAIN:" line
                .takeWhile { line ->
                    !line.trim().startsWith("android.") && !line.trim().endsWith(":") && line.trim().isNotEmpty()
                }.filter { line ->
                    line.trim().isNotEmpty() && line.contains("/") && line.matches(Regex("\\s*\\w+\\s+.+/.+"))
                }.mapNotNull { line ->
                    try {
                        val cleanLine = line.trim().replaceFirst(Regex("^\\w+\\s+"), "")
                        if (cleanLine.contains('/')) {
                            val (pkg, activity) = cleanLine.split('/', limit = 2)
                            ActivityInfo(pkg.trim(), activity.trim())
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.distinct()
                .sortedBy { "${it.packageName}/${it.activityPath}" }

        return activities
    }
}
