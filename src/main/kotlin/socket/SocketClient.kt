package socket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket


class SocketClient {
    companion object {
        const val SERVER_PORT: Int = 9500
        const val SERVER_IP: String = "localhost"
    }

    private lateinit var mClientSocket: Socket
    private lateinit var input: BufferedReader
    private lateinit var output: BufferedWriter

    private var isClosed = false

    private val isLive
        get() = mClientSocket.isConnected && !mClientSocket.isClosed && !isClosed

    fun connect(ip: String, port: Int): Flow<String> {
        isClosed = false
        return try {
            mClientSocket = Socket(ip, port);
            input = mClientSocket.getInputStream().bufferedReader()
            output = mClientSocket.getOutputStream().bufferedWriter()
            println("Waiting server response...");
            channelFlow {
                println("Connection stream")
                launch {
                    kotlinx.coroutines.delay(500)
                    this@SocketClient.send("wakeup/1")
                }
                while (isLive) {
                    input
                        .readLine()
                        ?.takeIf { it.isNotEmpty() }
                        ?.run {
                            println(this)
                            send(this)
                        }
                }
                println("Disconnected!")
            }.flowOn(Dispatchers.IO)
        } catch (e: Exception) {
            println("Connection fail");
            e.printStackTrace()
            close()
            channelFlow<String> { cancel("Connection fail", e) }.flowOn(Dispatchers.IO)
        }
    }

    fun send(command: String) {
        output.run {
            write("$command\n")
            flush()
            println("Sent $command")
        }
    }

    fun close() {
        isClosed = true
        try {
            mClientSocket.close()
        } catch (e: Throwable) {
        }
        try {
            input.close()
        } catch (e: Throwable) {
        }
        try {
            output.close()
        } catch (e: Throwable) {
        }
    }
}
