package plugins.device

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.WindowResult
import ui.OnAction

class DevicePlugin : plugins.Plugin<Any> {
    override val id: String = "DEVICE"
    override val name: String = "Device"
    override val icon: ImageVector = Icons.Filled.Smartphone
    override val middleware: IMiddleware<AppState> = DevicePluginMiddleware(id)

    override fun isResponsibleFor(action: IAction): Boolean = false

    @Composable
    override fun present(result: WindowResult<Any>, onAction: OnAction) {
        // Empty for now
    }
}
