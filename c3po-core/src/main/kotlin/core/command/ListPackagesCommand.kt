package core.command

import core.model.AppPackage

class ListPackagesCommand : EnhancedAdbCommand<List<AppPackage>> {
    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "dumpsys package",
            executionType = CommandExecutionType.SYSTEM_DUMP,
            timeoutMs = 30000L, // 30 seconds for dumpsys commands
            requiresShell = true,
        )

    override fun parse(result: String): List<AppPackage> {
        val packagesIndex = result.indexOf("Packages:")
        if (packagesIndex == -1) {
            return emptyList()
        }

        val packagesSection = result.substring(packagesIndex)

        // Simple line-by-line parsing based on the exact format we observed:
        // "  Package [package.name] (hash):"
        // "    userId=..."
        // "    pkg=..."
        // "    ...other lines..."
        // "    versionCode=5 targetSdk=34"
        // "    versionName=1.0.2"

        val packages = mutableListOf<AppPackage>()
        val lines = packagesSection.lines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Look for package line: "  Package [package.name] (hash):"
            if (line.trim().startsWith("Package [") && line.contains("]")) {
                val packageNameMatch = Regex("Package\\s*\\[([^\\]]+)\\]").find(line)
                if (packageNameMatch != null) {
                    val packageName = packageNameMatch.groupValues[1]

                    // Look ahead for package info in the next few lines
                    var versionCode = -1
                    var targetSdk = -1
                    var versionName = ""
                    var isSystemApp = false
                    var isDebuggable = false
                    var isEnabled = true
                    var installPath: String? = null

                    // Search the next 40 lines for package info
                    for (j in (i + 1) until minOf(i + 41, lines.size)) {
                        val nextLine = lines[j]

                        // Stop if we hit another package
                        if (nextLine.trim().startsWith("Package [")) {
                            break
                        }

                        // Look for versionCode and targetSdk on the same line
                        val versionCodeMatch = Regex("versionCode=(\\d+)").find(nextLine)
                        val targetSdkMatch = Regex("targetSdk=(\\d+)").find(nextLine)

                        if (versionCodeMatch != null) {
                            versionCode = versionCodeMatch.groupValues[1].toIntOrNull() ?: -1
                        }
                        if (targetSdkMatch != null) {
                            targetSdk = targetSdkMatch.groupValues[1].toIntOrNull() ?: -1
                        }

                        // Look for versionName
                        val versionNameMatch = Regex("versionName=(.*)").find(nextLine)
                        if (versionNameMatch != null) {
                            versionName = versionNameMatch.groupValues[1].trim()
                        }

                        // Look for install path (system apps typically in /system/)
                        val pathMatch = Regex("codePath=(.*)").find(nextLine)
                        if (pathMatch != null) {
                            installPath = pathMatch.groupValues[1].trim()
                            // System apps are typically installed in /system/ or /vendor/
                            isSystemApp = installPath?.startsWith("/system/") == true ||
                                    installPath?.startsWith("/vendor/") == true ||
                                    installPath?.startsWith("/product/") == true
                        }

                        // Look for debuggable flag in applicationInfo
                        if (nextLine.contains("applicationInfo") && nextLine.contains("debuggable")) {
                            isDebuggable = true
                        }

                        // Look for enabled/disabled state
                        if (nextLine.contains("enabled=") && nextLine.contains("false")) {
                            isEnabled = false
                        }
                    }

                    packages.add(
                        AppPackage(
                            packageName = packageName,
                            versionName = versionName,
                            versionCode = versionCode,
                            targetSdk = targetSdk,
                            isSystemApp = isSystemApp,
                            isDebuggable = isDebuggable,
                            isEnabled = isEnabled,
                            installPath = installPath,
                        ),
                    )
                }
            }
            i++
        }

        return packages.sortedBy { it.packageName }
    }
}
