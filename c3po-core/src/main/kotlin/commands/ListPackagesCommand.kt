package commands

import models.AppPackage

class ListPackagesCommand : EnhancedAdbCommand<List<AppPackage>> {
    override val commandSpec: CommandSpec = CommandSpec(
        baseCommand = "dumpsys package",
        executionType = CommandExecutionType.SYSTEM_DUMP,
        timeoutMs = 30000L, // 30 seconds for dumpsys commands
        requiresShell = true
    )

    override fun parse(result: String): List<AppPackage> {
        val packageLinePart = "\\s{2}Package\\s\\[(.*)\\].*"
        val ignoredLinesPart = "(?:\\r?\\n\\s{4}.*)*"
        val versionCodeAndTargetPart = "\\r?\\n\\s{4}versionCode=(\\d+).*\\stargetSdk=(\\d+)"
        val versionNamePart = "\\r?\\n\\s{4}versionName=(.*)"
        val regex = Regex("$packageLinePart$ignoredLinesPart$versionCodeAndTargetPart$ignoredLinesPart$versionNamePart")

        val packagesIndex = result.indexOf("Packages:")
        if (packagesIndex == -1) {
            println("DEBUG: Packages section not found in dumpsys output")
            println("DEBUG: Output length: ${result.length}")
            println("DEBUG: First 500 chars: ${result.take(500)}")
            return emptyList()
        }

        val packagesSection = result.substring(packagesIndex)

        return regex.findAll(packagesSection)
            .map { matchResult ->
                AppPackage(
                    packageName = matchResult.groupValues[1],
                    versionName = matchResult.groupValues[4],
                    versionCode = matchResult.groupValues[2].toIntOrNull() ?: -1,
                    targetSdk = matchResult.groupValues[3].toIntOrNull() ?: -1
                )
            }
            .sortedBy { it.packageName }
            .toList()
    }
}
