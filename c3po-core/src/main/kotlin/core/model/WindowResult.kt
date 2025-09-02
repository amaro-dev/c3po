package core.model

data class WindowResult<out T>(
    val searchTerm: String,
    val result: List<T>,
    val filterState: Map<String, Any> = emptyMap(),
)

