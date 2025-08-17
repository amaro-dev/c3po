package core.command

import core.model.AdbDevice

/**
 * Strategy interface for executing ADB commands in different environments.
 * This allows the same command logic to work across desktop ADB and Android Studio's ADB integration.
 */
interface CommandExecutionStrategy {
    /**
     * Execute a command on the specified device and return the raw output.
     *
     * @param device The target device
     * @param commandSpec The command specification containing all necessary information
     * @return The raw command output
     */
    suspend fun executeCommand(
        device: AdbDevice,
        commandSpec: CommandSpec,
    ): Result<String>
}

/**
 * Command specification that contains all information needed to execute a command
 * in any environment, without being tied to a specific execution method.
 */
data class CommandSpec(
    /**
     * The base command without environment-specific prefixes.
     * Example: "dumpsys package" (not "shell dumpsys package")
     */
    val baseCommand: String,
    /**
     * The type of command execution required
     */
    val executionType: CommandExecutionType,
    /**
     * Optional timeout in milliseconds
     */
    val timeoutMs: Long = 10000L,
    /**
     * Whether this command requires shell access
     */
    val requiresShell: Boolean = true,
)

/**
 * Types of command execution to help strategies determine how to execute commands
 */
enum class CommandExecutionType {
    /**
     * Standard ADB commands that don't require shell access (e.g., "devices", "forward")
     */
    ADB_DIRECT,

    /**
     * Shell commands that need to be executed within the device shell
     */
    SHELL_COMMAND,

    /**
     * Package manager commands
     */
    PACKAGE_MANAGER,

    /**
     * Activity manager commands
     */
    ACTIVITY_MANAGER,

    /**
     * System service dumps
     */
    SYSTEM_DUMP,
}
