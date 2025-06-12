package commands

import models.ActivityInfo

class ListActivitiesCommand : EnhancedAdbCommand<List<ActivityInfo>> {
    override val commandSpec: CommandSpec = CommandSpec(
        baseCommand = "dumpsys package",
        executionType = CommandExecutionType.SYSTEM_DUMP,
        timeoutMs = 30000L, // 30 seconds for dumpsys commands
        requiresShell = true
    )

    private fun log(message: String) {
        // Use System.err for logging since it's more likely to be captured
        System.err.println("[ListActivitiesCommand] $message")
    }

    override fun parse(result: String): List<ActivityInfo> {
        val lineSplitRule = Regex("\\r?\\n")
        var content = result.trim()

        // Log the parsing process
        log("Starting parse. Input length: ${result.length}")

        // Original parsing logic - must find Activity Resolver Table
        val activityResolverIndex = content.indexOf("Activity Resolver Table")
        if (activityResolverIndex == -1) {
            log("ERROR: Activity Resolver Table not found in dumpsys output")
            log("Output length: ${result.length}")
            log("First 500 chars: ${result.take(500)}")
            return emptyList()
        }

        log("Found Activity Resolver Table at index: $activityResolverIndex")
        content = content.substring(activityResolverIndex)

        val nonDataActionsIndex = content.indexOf("Non-Data Actions:")
        if (nonDataActionsIndex == -1) {
            log("ERROR: Non-Data Actions not found")
            log("Content length after Activity Resolver Table: ${content.length}")
            log("First 1000 chars after Activity Resolver Table: ${content.take(1000)}")
            return emptyList()
        }

        log("Found Non-Data Actions at index: $nonDataActionsIndex")
        content = content.substring(nonDataActionsIndex)

        val mainActionIndex = content.indexOf("android.intent.action.MAIN:")
        if (mainActionIndex == -1) {
            log("ERROR: android.intent.action.MAIN not found")
            log("Content length after Non-Data Actions: ${content.length}")
            log("First 1000 chars after Non-Data Actions: ${content.take(1000)}")
            return emptyList()
        }

        log("Found android.intent.action.MAIN at index: $mainActionIndex")
        content = content.substring(mainActionIndex)

        // Debug: show the content around MAIN action
        log("Content after android.intent.action.MAIN (first 1000 chars): ${content.take(1000)}")

        val lines = lineSplitRule.split(content)

        log("Total lines after split: ${lines.size}")
        log("First 10 lines after split:")
        lines.take(10).forEachIndexed { index, line ->
            log("Line $index: '${line}' (starts with 8 spaces: ${line.startsWith("        ")})")
        }

        // The issue is that after substring(), the lines lose their original indentation
        // We need to look for lines that contain activity patterns instead of relying on indentation
        val activities = lines
            .drop(1) // Skip the "android.intent.action.MAIN:" line
            .takeWhile { line ->
                // Continue while we have activity lines or empty lines
                // Stop when we hit another action or section
                !line.trim().startsWith("android.") && !line.trim().endsWith(":") && line.trim().isNotEmpty()
            }
            .filter { line ->
                // Filter for lines that look like activities: contain "/" and have a hash prefix
                line.trim().isNotEmpty() && line.contains("/") && line.matches(Regex("\\s*\\w+\\s+.+/.+"))
            }
            .mapNotNull { line ->
                try {
                    log("Processing line: '$line'")
                    // Remove the hash prefix (e.g., "c587056 ") from the beginning
                    val cleanLine = line.trim().replaceFirst(Regex("^\\w+\\s+"), "")
                    log("Clean line: '$cleanLine'")

                    if (cleanLine.contains('/')) {
                        val (pkg, activity) = cleanLine.split('/', limit = 2)
                        val activityInfo = ActivityInfo(pkg.trim(), activity.trim())
                        log("Created ActivityInfo: $activityInfo")
                        activityInfo
                    } else {
                        log("Clean line does not contain '/': '$cleanLine'")
                        null
                    }
                } catch (e: Exception) {
                    log("Failed to parse activity line: $line - Error: ${e.message}")
                    null
                }
            }
            .distinct()
            .sortedBy { "${it.packageName}/${it.activityPath}" }

        log("Parsing completed. Found ${activities.size} activities")
        if (activities.isNotEmpty()) {
            log("First few activities: ${activities.take(3)}")
        }

        return activities
    }
}
