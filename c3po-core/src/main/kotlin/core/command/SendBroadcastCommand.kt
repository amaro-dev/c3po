package core.command

import core.model.BroadcastAction

/**
 * Command to send a broadcast Intent to an Android device.
 * Supports optional extras as key-value pairs.
 */
class SendBroadcastCommand(
    private val broadcastAction: BroadcastAction,
    private val extras: Map<String, String> = emptyMap()
) : EnhancedAdbCommand<String> {

    override val commandSpec = CommandSpec(
        baseCommand = buildAmBroadcastCommand(),
        executionType = CommandExecutionType.ACTIVITY_MANAGER,
        timeoutMs = 10000L,
        requiresShell = true
    )

    private fun buildAmBroadcastCommand(): String {
        val baseCmd = "am broadcast -a \"${broadcastAction.action}\""

        // Add extras if provided
        val extrasString = if (extras.isNotEmpty()) {
            extras.entries.joinToString(" ") { (key, value) ->
                "--es \"$key\" \"$value\""
            }
        } else {
            ""
        }

        return if (extrasString.isNotEmpty()) {
            "$baseCmd $extrasString"
        } else {
            baseCmd
        }
    }

    /**
     * Parse the am broadcast output to determine success/failure.
     * Success typically shows "Broadcasting: Intent { ... }"
     */
    override fun parse(result: String): String {
        val trimmedResult = result.trim()

        return when {
            trimmedResult.contains("Broadcasting: Intent") -> "Broadcast sent successfully"
            trimmedResult.contains("Permission denied") -> "Permission denied: ${broadcastAction.requiredPermission ?: "unknown permission"}"
            trimmedResult.contains("Error") -> "Error: $trimmedResult"
            trimmedResult.isEmpty() -> "Broadcast sent (no output)"
            else -> "Result: $trimmedResult"
        }
    }
}