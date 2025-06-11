package core

import debug
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import socket.SocketClient
import socket.SocketDriver
import socket.SocketEvent

class SocketMiddleware(
    private val socketClient: SocketDriver,
    private val scope: CoroutineScope
) : IMiddleware<AppState> {


    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.SelectDevice -> {
                socketClient.reset()
            }

            is Action.SendSocketRequest -> {
                socketClient.send(action.command, action.arg)
            }

            is Action.Companion.Connect -> {
                scope.launch {
                    debug("Trying to connect ${SocketClient.SERVER_IP}:${SocketClient.SERVER_PORT}")
                    socketClient
                        .connect()
                        .collect {
                            when (it) {
                                SocketEvent.Connected ->
                                    processor.reduce(Action.Companion.UpdateState(state.companionState.setIsOnline()))

                                SocketEvent.Disconnected -> {
                                    processor.perform(Action.RefreshDevices)
                                    processor.reduce(Action.Companion.UpdateState(state.companionState.setIsOffline()))
                                }

                                is SocketEvent.Message -> {
                                    processor.perform(Action.DeliverSocketResponse(it.command, it.content))
                                }
                            }
                        }
                }
            }
        }
    }
}
