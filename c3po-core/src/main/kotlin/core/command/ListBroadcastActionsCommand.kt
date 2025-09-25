package core.command

import core.broadcast.BroadcastActionParser
import core.model.BroadcastAction

/**
 * Command to list broadcast actions that can be sent on an Android device.
 * Executes `adb shell dumpsys activity broadcasts` and parses registered receivers.
 */
class ListBroadcastActionsCommand : EnhancedAdbCommand<List<BroadcastAction>> {

    private val parser = BroadcastActionParser()

    override val commandSpec = CommandSpec(
        baseCommand = "dumpsys activity broadcasts",
        executionType = CommandExecutionType.SYSTEM_DUMP,
        timeoutMs = 15000L, // Longer timeout for system dumps
        requiresShell = true
    )

    /**
     * Parse the dumpsys output to extract broadcast actions.
     * Only processes registered receivers, ignoring sticky broadcasts.
     */
    override fun parse(result: String): List<BroadcastAction> {
        return parser.parse(result)
    }
}