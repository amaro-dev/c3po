package core.command

import core.model.ActivityInfo

class QueryLauncherActivitiesCommand : EnhancedAdbCommand<List<ActivityInfo>> {
    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.HOME --brief --user 0",
            executionType = CommandExecutionType.PACKAGE_MANAGER,
            timeoutMs = 10000L,
            requiresShell = true,
        )

    override fun parse(result: String): List<ActivityInfo> {
        val lines = result.split('\n')
        val activities = mutableListOf<ActivityInfo>()

        for ((_, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            // Look for lines that contain package/activity format directly
            if (trimmedLine.contains('/') && !trimmedLine.contains("Activity #") && !trimmedLine.contains("priority=")) {
                try {
                    val parts = trimmedLine.split('/')
                    if (parts.size >= 2) {
                        val packageName = parts[0].trim()
                        val activityPath = parts[1].trim()
                        activities.add(ActivityInfo(packageName, activityPath, isLauncherCapable = true))
                    }
                } catch (e: Exception) {
                    // Skip malformed lines
                }
            }
        }

        return activities.distinct()
    }
}