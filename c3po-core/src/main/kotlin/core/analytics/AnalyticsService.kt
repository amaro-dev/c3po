package core.analytics

import core.util.AppPaths
import ly.count.sdk.java.Config
import ly.count.sdk.java.Countly
import org.slf4j.LoggerFactory

/**
 * Simple facade for Countly analytics integration.
 * Provides basic event tracking with graceful error handling.
 */
class AnalyticsService {
    private val logger = LoggerFactory.getLogger("AnalyticsService")
    private var isInitialized = false
    private val pendingEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    /**
     * Initialize Countly with minimal configuration for testing
     */
    fun initialize(serverUrl: String, appKey: String) {
        try {
            val normalizedUrl = serverUrl.trimEnd('/')
            val storageDir = AppPaths.getAnalyticsDirectory()
            val config = Config(normalizedUrl, appKey, storageDir)
                .setLoggingLevel(Config.LoggingLevel.DEBUG)
                .enableFeatures(
                    Config.Feature.Events,
                    Config.Feature.Sessions
                )

            Countly.instance().init(config)
            isInitialized = true
            logger.info("Analytics initialized with server: $normalizedUrl, dir: ${storageDir.absolutePath}")

            // Drain any events recorded before initialization completed
            drainPendingEvents()
        } catch (e: Exception) {
            logger.error("Failed to initialize analytics: ${e.message}")
            isInitialized = false
        }
    }

    /**
     * Track a simple custom event
     */
    fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        if (!isInitialized) {
            synchronized(pendingEvents) { pendingEvents.add(eventName to properties) }
            logger.debug("Analytics not initialized, queued event: $eventName")
            return
        }

        try {
            if (properties.isEmpty()) {
                Countly.instance().events().recordEvent(eventName)
            } else {
                Countly.instance().events().recordEvent(eventName, properties)
            }
            logger.debug("Tracked event: $eventName with properties: $properties")
        } catch (e: Exception) {
            logger.error("Failed to track event '$eventName': ${e.message}")
        }
    }

    /**
     * Shutdown analytics
     */
    fun shutdown() {
        if (isInitialized) {
            try {
                Countly.instance().halt()
                isInitialized = false
                logger.info("Analytics shutdown completed")
            } catch (e: Exception) {
                logger.error("Error during analytics shutdown: ${e.message}")
            }
        }
    }

    private fun drainPendingEvents() {
        val toSend: List<Pair<String, Map<String, Any>>> = synchronized(pendingEvents) {
            if (pendingEvents.isEmpty()) return
            val copy = pendingEvents.toList()
            pendingEvents.clear()
            copy
        }
        toSend.forEach { (name, props) ->
            try {
                if (props.isEmpty()) {
                    Countly.instance().events().recordEvent(name)
                } else {
                    Countly.instance().events().recordEvent(name, props)
                }
                logger.debug("Drained queued event: $name with properties: $props")
            } catch (e: Exception) {
                logger.error("Failed to send queued event '$name': ${e.message}")
            }
        }
    }
}
