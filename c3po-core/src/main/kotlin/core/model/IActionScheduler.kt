package core.model

import dev.amaro.sonic.IAction

interface IActionScheduler {
    fun schedule(action: IAction)
}
