package core.command

import core.model.AdbDevice

/**
 * Unified command executor that can work with different execution strategies.
 * This allows the same command logic to work across desktop and Android Studio environments.
 */
class UnifiedCommandExecutor(
    private val executionStrategy: CommandExecutionStrategy,
) {
    /**
     * Execute a command using the configured strategy.
     * Supports both legacy AdbCommand and enhanced EnhancedAdbCommand interfaces.
     */
    suspend fun <T> execute(
        command: AdbCommand<T>,
        device: AdbDevice,
    ): Result<T> =
        try {
            val result =
                when (command) {
                    is EnhancedAdbCommand<T> -> {
                        // Use the new strategy-based execution
                        executionStrategy.executeCommand(device, command.commandSpec)
                    }

                    else -> {
                        // Fallback to legacy execution for backward compatibility
                        executeLegacyCommand(command, device)
                    }
                }

            if (result.isSuccess) {
                val output = result.getOrThrow()
                val parsed = command.parse(output)
                Result.success(parsed)
            } else {
                Result.failure(result.exceptionOrNull() ?: RuntimeException("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Legacy command execution for backward compatibility
     */
    private suspend fun <T> executeLegacyCommand(
        command: AdbCommand<T>,
        device: AdbDevice,
    ): Result<String> {
        // Create a default CommandSpec for legacy commands
        val commandSpec =
            CommandSpec(
                baseCommand = command.command.removePrefix("shell "),
                executionType =
                    when {
                        command.command.contains("dumpsys") -> CommandExecutionType.SYSTEM_DUMP
                        command.command.contains("pm ") -> CommandExecutionType.PACKAGE_MANAGER
                        command.command.contains("am ") -> CommandExecutionType.ACTIVITY_MANAGER
                        command.command.startsWith("shell ") -> CommandExecutionType.SHELL_COMMAND
                        else -> CommandExecutionType.ADB_DIRECT
                    },
                timeoutMs = 10000L,
                requiresShell = command.command.startsWith("shell "),
            )

        return executionStrategy.executeCommand(device, commandSpec)
    }
}
