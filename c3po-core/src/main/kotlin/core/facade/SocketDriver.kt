package core.facade

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
        val request =
            if (arg != null) {
                "$command: $arg/${UUID.randomUUID()}"
            } else {
                "$command/${UUID.randomUUID()}"
            }
        client.send(request)
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
                    flowOf(
                        *aggregator
                            .readyToDeliver()
                            .map { msg -> SocketEvent.Message(msg.first, msg.second) }
                            .toTypedArray(),
                    )
                }
            }
}
