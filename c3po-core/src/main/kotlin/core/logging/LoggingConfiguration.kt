package core.logging

import Settings
import java.util.Properties

/**
 * Configuration helper for the logging system.
 * Reads settings and provides typed access to logging configuration.
 */
class LoggingConfiguration(private val settings: Properties) {

    fun isLoggingEnabled(): Boolean {
        return settings.getProperty(Settings.LOGGING_ENABLED_PROP, "false").toBoolean()
    }

    fun isPerformLoggingEnabled(): Boolean {
        return settings.getProperty(Settings.LOGGING_PERFORM_PROP, "false").toBoolean()
    }

    fun isReduceLoggingEnabled(): Boolean {
        return settings.getProperty(Settings.LOGGING_REDUCE_PROP, "false").toBoolean()
    }

}