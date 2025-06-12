package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

/**
 * Action to connect to selected device.
 * This can be invoked from toolbar or shortcut.
 */
class ConnectDeviceAction : AnAction() {

    companion object {
        private val LOG = Logger.getInstance(ConnectDeviceAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("Connect device action triggered")

        // TODO: Implement device connection logic
        // This could show device selection dialog or connect to current selection
    }
}
