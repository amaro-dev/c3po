package core.broadcast

import core.model.BroadcastAction

/**
 * Parser for extracting broadcast actions from `adb shell dumpsys activity broadcasts` output.
 * Focuses only on registered receivers, ignoring sticky broadcasts per requirements.
 */
class BroadcastActionParser {

    /**
     * Pattern to extract package name from ReceiverList line.
     * Format: ReceiverList{id pid package/uid/user [remote:id]}
     * Examples:
     * - ReceiverList{e1265c2 2146 com.google.android.gms.persistent/10008/u0 remote:254e20d}
     * - ReceiverList{850a9cc 1298 system/1000/u-1 local:c94e7ff}
     */
    private val receiverListPattern = Regex("""ReceiverList\{[^}]+\s+\d+\s+([^/\s]+)""")

    /**
     * Pattern to extract action from Action line.
     * Format: Action: "action.name"
     */
    private val actionPattern = Regex("""Action:\s*"([^"]+)"""")

    /**
     * Pattern to extract required permission.
     * Format: requiredPermission=permission.name
     */
    private val permissionPattern = Regex("""requiredPermission=([^\s]+)""")

    /**
     * Parse dumpsys activity broadcasts output to extract registered broadcast actions.
     *
     * @param dumpsysOutput Raw output from `adb shell dumpsys activity broadcasts`
     * @return List of BroadcastAction representing action-package combinations
     */
    fun parse(dumpsysOutput: String): List<BroadcastAction> {
        val results = mutableListOf<BroadcastAction>()
        val lines = dumpsysOutput.lines()

        // Skip everything until we find "Registered Receivers:"
        val registeredStart = lines.indexOfFirst { it.contains("Registered Receivers:") }
        if (registeredStart == -1) {
            return emptyList()
        }

        // Stop parsing when we hit sticky broadcasts or end of data
        val stickyStart = lines.indexOfFirst {
            it.contains("Sticky broadcasts") || it.contains("Historical broadcasts")
        }.takeIf { it > registeredStart } ?: lines.size

        // Parse only the registered receivers section
        val registeredLines = lines.subList(registeredStart + 1, stickyStart)

        var currentPackage: String? = null
        val currentFilterActions = mutableListOf<String>()
        var currentFilterPermission: String? = null

        fun flushCurrentFilter() {
            // Create BroadcastAction for each action in the current filter
            if (currentPackage != null && currentFilterActions.isNotEmpty()) {
                for (action in currentFilterActions) {
                    results.add(
                        BroadcastAction(
                            action = action,
                            packageName = currentPackage!!,
                            requiredPermission = currentFilterPermission
                        )
                    )
                }
            }
            currentFilterActions.clear()
            currentFilterPermission = null
        }

        for (line in registeredLines) {
            when {
                // New ReceiverList - flush previous filter and extract package name
                line.contains("ReceiverList{") -> {
                    flushCurrentFilter()
                    val packageMatch = receiverListPattern.find(line)
                    currentPackage = packageMatch?.groups?.get(1)?.value
                }

                // New Filter - flush previous filter and reset
                line.contains("Filter #") -> {
                    flushCurrentFilter()
                }

                // Extract permission for current filter
                line.contains("requiredPermission=") -> {
                    val permissionMatch = permissionPattern.find(line)
                    currentFilterPermission = permissionMatch?.groups?.get(1)?.value
                }

                // Extract action - add to current filter actions
                line.contains("Action:") -> {
                    val actionMatch = actionPattern.find(line)
                    val action = actionMatch?.groups?.get(1)?.value
                    if (action != null) {
                        currentFilterActions.add(action)
                    }
                }
            }
        }

        // Flush the last filter
        flushCurrentFilter()

        return results
    }
}