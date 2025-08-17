package core.facade

import debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.net.Socket

class SocketClient {
    companion object {
        const val SERVER_PORT: Int = 9500
        const val SERVER_IP: String = "localhost"
    }

    private lateinit var mClientSocket: Socket
    private lateinit var input: BufferedReader
    private lateinit var output: BufferedWriter
    private var isClosed = true

    val isLive: Boolean
        get() =
            this::mClientSocket.isInitialized &&
                    mClientSocket.isConnected &&
                    !mClientSocket.isClosed &&
                    !isClosed &&
                    mClientSocket.isBound

    fun connect(
        ip: String = SERVER_IP,
        port: Int = SERVER_PORT,
    ): Flow<String> =
        flow {
            try {
                mClientSocket = Socket(ip, port)
                input = mClientSocket.getInputStream().bufferedReader()
                output = mClientSocket.getOutputStream().bufferedWriter()
                isClosed = false

                // Initial wakeup message
                kotlinx.coroutines.delay(500)
                send("wakeup/1")

                while (isLive) {
                    val message = input.readLine()
                    if (message != null) {
                        emit(message)
                    } else {
                        break
                    }
                }
            } catch (e: Exception) {
                println("Connection fail: ${e.message}")
                emit("Connection fail: ${e.message}")
                close()
            } finally {
                close()
                emit("DISCONNECTED")
            }
        }.flowOn(Dispatchers.IO)

    fun send(command: String): Boolean {
        if (isClosed || !isLive) {
            debug("Connection is not open!")
            return false
        }
        try {
            output.write("$command\n")
            output.flush()
            debug("Sent: $command")
            return true
        } catch (e: IOException) {
            println("Failed to send: ${e.message}")
            close()
            return false
        }
    }

    fun close() {
        isClosed = true
        try {
            mClientSocket.close()
            input.close()
            output.close()
            debug("Connection closed")
        } catch (e: Throwable) {
            println("Error closing resources: ${e.message}")
        }
    }
}
