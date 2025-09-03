package core.logging

import java.time.Instant

/**
 * Represents a structured log event in the C3PO logging system.
 *
 * @param ts ISO timestamp when the event occurred
 * @param cat Log category: "adb", "perform", or "reduce"
 * @param event Name of the action or command (e.g., "RefreshDevices", "ListPackagesCommand")
 * @param msg Human readable message with context and results
 */
data class LogEvent(
    val ts: String,
    val cat: String,
    val event: String,
    val msg: String,
) {
    companion object {
        fun create(category: String, event: String, message: String): LogEvent {
            return LogEvent(
                ts = Instant.now().toString(),
                cat = category,
                event = event,
                msg = message
            )
        }
    }
}