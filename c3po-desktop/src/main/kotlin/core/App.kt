package core

import core.facade.SocketClient
import core.model.Action
import core.model.AppStateManager
import dev.amaro.sonic.IAction
import di.Names
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import plugins.Plugin

class App : KoinComponent {
    private val socketClient: SocketClient = get()

    val plugins: List<Plugin<*>> = get(named(Names.PLUGIN_LIST_DEPENDENCY))

    private val stateManager: AppStateManager = get()

    fun start() {
        perform(Action.LoadSettings)
    }

    fun perform(action: IAction) = stateManager.perform(action)

    fun listen() = stateManager.listen()

    fun exit() {
        socketClient.close()
    }
}
