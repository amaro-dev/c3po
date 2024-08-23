package core

import Settings
import commands.CommandExecutor
import dev.amaro.sonic.ConditionedDirectMiddleware
import dev.amaro.sonic.IAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import socket.SocketClient
import ui.plugins.Plugin
import ui.plugins.activities.ActivitiesPlugin
import ui.plugins.attrs.DeviceAttrsPlugin
import ui.plugins.intents.pending.PendingIntentsPlugin
import ui.plugins.packages.PackagesPlugin
import ui.plugins.services.ServicesPlugin
import java.awt.datatransfer.Clipboard
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class App(clipboard: Clipboard) {

   // private val socketClient = SocketClient()

    private val executor = CommandExecutor()

    val plugins: List<Plugin<*>> = listOf(
        ActivitiesPlugin(executor),
        PackagesPlugin(executor),
        DeviceAttrsPlugin(executor),
        ServicesPlugin(executor),
        PendingIntentsPlugin(executor)
    )

    private val resourcesPath =
        if (Settings.isDebug())
            File(Paths.get("build").absolutePathString())
        else
            File(Settings.productionSettingsFolder()).apply {
                if (!exists()) Files.createDirectory(toPath())
            }

    private val stateManager = AppStateManager(
        CommandMiddleware(executor),
        PluginSelectorMiddleware(plugins),
        ClipboardMiddleware(clipboard),
        ConditionedDirectMiddleware(
            Action.SelectPlugin::class,
            Action.ClosePlugin::class,
            Action.ChangeFilter::class,
            Action.ClearError::class
        ),
        SettingsMiddleware(resourcesPath, Settings.FILE_NAME),
        CompanionMiddleware(),
        //SocketMiddleware(socketClient)
    )

    fun start() {
        perform(Action.LoadSettings)
//        val aggregator = SocketResponseAggregator()
//        CoroutineScope(Dispatchers.IO).launch {
//            println("Connecting...")
//            socketClient.connect(SocketClient.SERVER_IP, SocketClient.SERVER_PORT).collect {
//                aggregator.parse(it)
//                aggregator.readyToDeliver().forEach {
//                    perform(Action.DeliverSocketResponse(it.first, it.second))
//                }
//            }
//        }
    }

    fun perform(action: IAction) = stateManager.perform(action)

    fun listen() = stateManager.listen()

    fun exit() {
        //socketClient.close()
    }
}

//class SocketResponseAggregator {
//
//    private val cache = HashMap<String, MutableList<String>>()
//
//    private val ready: MutableList<String> = mutableListOf()
//
//    fun parse(line: String) {
//        if (line.startsWith("BEGIN")) {
//            cache[line.split(' ')[1]] = mutableListOf()
//        } else if (line.startsWith("END")) {
//            ready.add(line.split(' ')[1])
//        } else {
//            cache[line.split(' ')[0]]?.add(line.substringAfter(' '))
//        }
//    }
//
//    fun readyToDeliver(): List<Pair<String, List<String>>> = ready.map { it to (cache[it] ?: emptyList()) }
//}
