package core.command

import core.model.SleepState

class GetPackageSleepStateCommand(
    private val packageName: String
) : EnhancedAdbCommand<SleepState> {
    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "dumpsys usagestats $packageName",
            executionType = CommandExecutionType.SYSTEM_DUMP,
            timeoutMs = 5000L, // 5 seconds for individual package usage stats
            requiresShell = true,
        )

    override fun parse(result: String): SleepState {
        // Look for the current standby bucket in App Standby States section
        // Format: package=com.example.app u=0 bucket=5 reason=d used=...
        // Escape the package name for regex since it contains dots
        val escapedPackageName = Regex.escape(packageName)
        val bucketMatch = Regex("package=$escapedPackageName.*?bucket=(\\d+)").find(result)

        return if (bucketMatch != null) {
            val bucket = bucketMatch.groupValues[1].toIntOrNull() ?: 50
            when (bucket) {
                5, 10, 20 -> SleepState.Awake    // EXEMPTED, ACTIVE, WORKING_SET
                30, 40, 50 -> SleepState.Asleep  // FREQUENT, RARE, NEVER
                else -> SleepState.Unknown
            }
        } else {
            SleepState.Unknown
        }
    }
}