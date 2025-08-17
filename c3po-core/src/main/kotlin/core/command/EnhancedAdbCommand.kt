package core.command

/**
 * Enhanced ADB command interface that supports multiple execution strategies.
 * This extends the existing AdbCommand interface for backward compatibility while
 * adding support for the new CommandSpec approach.
 */
interface EnhancedAdbCommand<T> : AdbCommand<T> {
    /**
     * The command specification that defines how this command should be executed
     * across different environments (desktop ADB vs Android Studio).
     */
    val commandSpec: CommandSpec

    /**
     * Default implementation that derives the legacy command string from commandSpec
     */
    override val command: String
        get() =
            if (commandSpec.requiresShell) {
                "shell ${commandSpec.baseCommand}"
            } else {
                commandSpec.baseCommand
            }
}
