package models

data class WindowResult<out T>(
    val searchTerm: String,
    val result: List<T>,
)
