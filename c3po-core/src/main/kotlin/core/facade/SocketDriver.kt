package core.facade

import debug
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class SocketDriver(
    private val client: SocketClient,
    private val aggregator: SocketResponseAggregator,
) {
    fun reset() {
        if (client.isLive) client.close()
    }

    fun send(
        command: String,
        arg: String?,
    ) {
        val uuid = UUID.randomUUID()
        val request =
            if (arg != null) {
                "$command: $arg/$uuid"
            } else {
                "$command/$uuid"
            }
        debug("Sending socket command: $request")
        val sent = client.send(request)
        if (!sent) {
            debug("Failed to send socket command: $request")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun connect(): Flow<SocketEvent> =
        client
            .connect(SocketClient.SERVER_IP, SocketClient.SERVER_PORT)
            .flatMapConcat {
                if (it.startsWith("CONNECTED")) {
                    flowOf(SocketEvent.Connected)
                } else if (it.startsWith("DISCONNECTED")) {
                    flowOf(SocketEvent.Disconnected)
                } else {
                    aggregator.parse(it)

                    // Check for timed out commands
                    val timedOut = aggregator.checkForTimedOutCommands()
                    val timeoutEvents = timedOut.map { id ->
                        debug("Creating timeout event for command: $id")
                        SocketEvent.Timeout(id)
                    }

                    // Deliver ready messages
                    val readyMessages = aggregator
                        .readyToDeliver()
                        .map { msg -> SocketEvent.Message(msg.first, msg.second) }

                    flowOf(*(timeoutEvents + readyMessages).toTypedArray())
                }
            }
}
