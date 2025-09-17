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
        val activities = parseActivitiesFromDumpsys(result)
        return enrichActivitiesWithLauncherInfo(activities)
    }

    private fun parseActivitiesFromDumpsys(result: String): List<ActivityInfo> {
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

        return lines
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
    }

    private fun enrichActivitiesWithLauncherInfo(activities: List<ActivityInfo>): List<ActivityInfo> {
        return try {
            // Query launcher activities to identify which ones are launcher-capable
            val launcherCommand = QueryLauncherActivitiesCommand()

            // For performance, we'll do a simple sync execution here
            val process = ProcessBuilder(
                "adb",
                "shell",
                "cmd",
                "package",
                "query-activities",
                "-a",
                "android.intent.action.MAIN",
                "-c",
                "android.intent.category.HOME",
                "--brief",
                "--user",
                "0"
            )
                .redirectErrorStream(true)
                .start()

            val launcherOutput = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                val launcherActivities = launcherCommand.parse(launcherOutput).map { it.fullPath }.toSet()

                // Enrich activities with launcher capability info
                activities.map { activity ->
                    activity.copy(isLauncherCapable = launcherActivities.contains(activity.fullPath))
                }
            } else {
                // If command failed, fallback: assume some well-known launchers might be present
                activities.map { activity ->
                    val isLauncher = activity.packageName.contains("launcher") ||
                            activity.packageName.contains("home") ||
                            activity.activityPath.contains("Launcher") ||
                            activity.activityPath.contains("Home")
                    activity.copy(isLauncherCapable = isLauncher)
                }
            }
        } catch (e: Exception) {
            // If launcher query fails completely, fallback: assume some well-known launchers might be present
            activities.map { activity ->
                val isLauncher = activity.packageName.contains("launcher") ||
                        activity.packageName.contains("home") ||
                        activity.activityPath.contains("Launcher") ||
                        activity.activityPath.contains("Home")
                activity.copy(isLauncherCapable = isLauncher)
            }
        }
    }
}
