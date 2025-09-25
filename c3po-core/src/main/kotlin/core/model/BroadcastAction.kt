package core.model

/**
 * Represents a broadcast action that can be sent on an Android device.
 * Each instance represents one action-package combination from dumpsys activity broadcasts output.
 */
data class BroadcastAction(
    /**
     * The broadcast action name (e.g., "android.intent.action.BOOT_COMPLETED")
     */
    val action: String,

    /**
     * The package name that handles this broadcast action
     */
    val packageName: String,

    /**
     * Required permission to send this broadcast to this specific receiver, if any
     */
    val requiredPermission: String? = null
)