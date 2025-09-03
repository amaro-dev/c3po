package core.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Non-blocking structured logger that writes events to files with rotation and retention.
 * Uses an async queue to avoid impacting application performance.
 */
class StructuredLogger(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val logger = LoggerFactory.getLogger("C3PO")
    private val eventQueue = Channel<LogEvent>(capacity = 10000)
    private val isRunning = AtomicBoolean(false)

    init {
        startEventProcessor()
    }

    /**
     * Logs an event asynchronously. If queue is full, the event is dropped silently.
     */
    fun log(event: LogEvent) {
        if (!isRunning.get()) return

        // Try to offer to queue, drop if full to avoid blocking
        eventQueue.trySend(event)
    }

    /**
     * Convenience method for logging events
     */
    fun log(category: String, event: String, message: String) {
        log(LogEvent.create(category, event, message))
    }

    /**
     * Starts the logger
     */
    fun start() {
        isRunning.set(true)
    }

    /**
     * Stops the logger and flushes remaining events
     */
    fun stop() {
        isRunning.set(false)
        eventQueue.close()
    }

    private fun startEventProcessor() {
        scope.launch {
            for (event in eventQueue) {
                try {
                    // Convert to JSON and write via SLF4J
                    val jsonLine = eventToJson(event)
                    logger.info(jsonLine)
                } catch (e: Exception) {
                    // Fallback to console if logging fails
                    println("[LOGGING ERROR] Failed to write log: ${e.message}")
                    println("[FALLBACK] ${event}")
                }
            }
        }
    }

    private fun eventToJson(event: LogEvent): String {
        // Simple JSON serialization without external dependencies
        return """{"ts":"${event.ts}","cat":"${event.cat}","event":"${event.event}","msg":"${escapeJson(event.msg)}"}"""
    }

    private fun escapeJson(str: String): String {
        return str.replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\\", "\\\\")
    }

    companion object {
        @Volatile
        private var INSTANCE: StructuredLogger? = null

        fun getInstance(): StructuredLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StructuredLogger().also { INSTANCE = it }
            }
        }
    }
}