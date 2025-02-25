package core

import debug
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import socket.SocketClient
import socket.SocketResponseAggregator
import java.util.UUID

class SocketMiddleware(
    private val socketClient: SocketClient,
) : IMiddleware<AppState> {
    private val aggregator = SocketResponseAggregator()

    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.SelectDevice -> {
                if (socketClient.isLive) socketClient.close()
            }
            is Action.SendSocketRequest -> {
                val request = if (action.arg != null) {
                    "${action.command}: ${action.arg}/${UUID.randomUUID()}"
                } else {
                    "${action.command}/${UUID.randomUUID()}"
                }
                socketClient.send(request)
            }

            is Action.Companion.Connect -> {
                CoroutineScope(Dispatchers.IO).launch {
                    debug("Trying to connect ${SocketClient.SERVER_IP}:${SocketClient.SERVER_PORT}")
                    socketClient
                        .connect(SocketClient.SERVER_IP, SocketClient.SERVER_PORT)
                        .onEach {
                            if (it == "CONNECTED") {
                                processor.reduce(Action.Companion.UpdateState(state.companionState.setIsOnline()))
                            }
                        }.filterNot { it == "CONNECTED" }
                        .onCompletion { processor.perform(Action.Companion.CheckInstalled) }
                        .collect {
                            aggregator.parse(it)
                            aggregator.readyToDeliver().forEach { msg ->
                                processor.perform(Action.DeliverSocketResponse(msg.first, msg.second))
                            }
                        }
                }
            }
        }
    }
}
