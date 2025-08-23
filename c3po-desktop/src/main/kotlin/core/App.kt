package core

import core.model.Action
import core.model.AppStateManager
import dev.amaro.sonic.IAction
import di.Names
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import plugins.Plugin

class App : KoinComponent {

    val plugins: List<Plugin<*>> = get(named(Names.PLUGIN_LIST_DEPENDENCY))

    private val stateManager: AppStateManager = get()

    fun start() {
        perform(Action.LoadSettings)
        perform(Action.CheckForUpdate) // Check for updates on startup
    }

    fun perform(action: IAction) = stateManager.perform(action)

    fun listen() = stateManager.listen()

    fun exit() {
        // Application cleanup if needed
    }
}
