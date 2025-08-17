package core.facade

sealed interface SocketEvent {
    data object Connected : SocketEvent

    data object Disconnected : SocketEvent

    data class Message(
        val command: CommandEntry,
        val content: List<String>,
    ) : SocketEvent
}
