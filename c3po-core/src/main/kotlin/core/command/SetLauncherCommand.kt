package core.command

import core.model.ActivityInfo

class SetLauncherCommand(
    activityInfo: ActivityInfo
) : EnhancedAdbCommand<Unit> {
    override val commandSpec: CommandSpec =
        CommandSpec(
            baseCommand = "cmd package set-home-activity ${activityInfo.fullPath}",
            executionType = CommandExecutionType.PACKAGE_MANAGER,
            timeoutMs = 5000L,
            requiresShell = true,
        )

    override fun parse(result: String) {
        // The command doesn't return meaningful output on success
        // Errors will be handled by the command execution framework
    }
}