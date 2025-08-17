package core.model

data class WindowResult<out T>(
    val searchTerm: String,
    val result: List<T>,
    val filterState: Map<String, Any> = emptyMap(),
)

// Extension functions for type-safe filter access
fun <T> WindowResult<T>.getBooleanFilter(key: String, default: Boolean = false): Boolean =
    (filterState[key] as? Boolean) ?: default

fun <T> WindowResult<T>.getStringFilter(key: String, default: String = ""): String =
    (filterState[key] as? String) ?: default

fun <T> WindowResult<T>.getIntFilter(key: String, default: Int = 0): Int =
    (filterState[key] as? Int) ?: default
