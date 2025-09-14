package core.command

/**
 * Command to get the actual APK path(s) for a package using pm path
 */
class GetPackageApkPathCommand(
    private val packageName: String
) : EnhancedAdbCommand<List<String>> {

    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "pm path $packageName",
            executionType = CommandExecutionType.PACKAGE_MANAGER,
            timeoutMs = 10000L,
            requiresShell = true,
        )

    override fun parse(result: String): List<String> {
        return result.lines()
            .filter { it.startsWith("package:") }
            .map { it.substringAfter("package:").trim() }
            .filter { it.isNotBlank() }
            .takeIf { it.isNotEmpty() }
            ?: throw RuntimeException("No APK paths found for package: $packageName")
    }
}