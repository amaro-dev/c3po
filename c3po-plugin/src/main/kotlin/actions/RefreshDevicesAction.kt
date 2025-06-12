package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

/**
 * Action to refresh connected Android devices.
 * This can be invoked from toolbar or shortcut.
 */
class RefreshDevicesAction : AnAction() {

    companion object {
        private val LOG = Logger.getInstance(RefreshDevicesAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        LOG.info("Refresh devices action triggered")

        // TODO: Implement device refresh logic
        // This could notify the tool window to refresh its device list
    }
}
