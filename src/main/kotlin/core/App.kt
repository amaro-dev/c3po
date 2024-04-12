package core

import dev.amaro.sonic.ConditionedDirectMiddleware

class App {
    private val stateManager = AppStateManager(
        CommandMiddleware(),
        ConditionedDirectMiddleware(
            Action.SelectTab::class
        )
    )

    fun perform(action: Action) = stateManager.perform(action)

    fun listen() = stateManager.listen()
}