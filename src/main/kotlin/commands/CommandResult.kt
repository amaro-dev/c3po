package commands

data class CommandResult(
    val content: String,
    val resultCode: Int,
    val error: String? = null
)
