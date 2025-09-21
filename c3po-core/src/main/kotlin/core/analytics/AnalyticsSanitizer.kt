package core.analytics

object AnalyticsSanitizer {
    fun sanitize(message: String): String {
        var s = message
        s = s.replace(Regex("/[\\w./-]+"), "/…")
        if (s.length > 100) s = s.substring(0, 100)
        return s
    }
}

