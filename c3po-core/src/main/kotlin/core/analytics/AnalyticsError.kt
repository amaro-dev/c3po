package core.analytics

import java.util.Locale

object AnalyticsError {
    fun categorize(message: String): String {
        val m = message.lowercase(Locale.getDefault())
        return when {
            listOf("timeout", "timed out").any { it in m } -> "timeout"
            listOf("permission", "denied", "forbidden").any { it in m } -> "permission"
            listOf("network", "connection", "resolve host", "dns").any { it in m } -> "network"
            listOf("parse", "format", "json").any { it in m } -> "parse"
            else -> "unknown"
        }
    }
}

