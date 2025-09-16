package plugins.device.definition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.command.CommandExecutor
import core.model.AppState
import core.model.DeviceInfo
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.device.structure.DevicePluginMiddleware
import plugins.device.ui.DeviceActionPanel
import plugins.device.ui.DeviceInfoCard
import plugins.device.ui.DiskUsageCard
import plugins.device.ui.toDisplayItems
import ui.OnAction

class DevicePlugin(
    executor: CommandExecutor
) : plugins.Plugin<DeviceInfo> {
    override val id: String = "DEVICE"
    override val name: String = "Device"
    override val icon: ImageVector = Icons.Filled.Smartphone
    override val middleware: IMiddleware<AppState> = DevicePluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean {
        return when (action) {
            is core.model.Action.TakeScreenshot -> true
            is core.model.Action.RestartDevice -> true
            is core.model.Action.ConfirmRestartDevice -> true
            is core.model.Action.DismissRestartConfirmation -> true
            else -> false
        }
    }

    @Composable
    override fun present(result: WindowResult<DeviceInfo>, onAction: OnAction) {
        val deviceInfo = result.result.firstOrNull()

        if (deviceInfo != null) {
            val cardGroups = deviceInfo.toDisplayItems()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top row: Device and Disk Usage cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DeviceInfoCard(
                        title = "Device",
                        items = cardGroups[0],
                        onAction = onAction,
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeviceActionPanel(onAction = onAction)

                        DiskUsageCard(
                            title = "Disk Usage",
                            diskStats = deviceInfo.status.diskUsage,
                            onAction = onAction
                        )
                    }
                }

                // Bottom row: System and Status cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DeviceInfoCard(
                        title = "System",
                        items = cardGroups[1],
                        onAction = onAction,
                        modifier = Modifier.weight(1f)
                    )
                    DeviceInfoCard(
                        title = "Status",
                        items = cardGroups[2],
                        onAction = onAction,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
