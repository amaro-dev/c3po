package commands

sealed interface ParsedResult

data object Success : ParsedResult

data class Error(val message: String): ParsedResult
