package plugins.device.definition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import plugins.device.ui.DeviceInfoCard
import plugins.device.ui.toDisplayItems
import ui.OnAction

class DevicePlugin(
    executor: CommandExecutor
) : plugins.Plugin<DeviceInfo> {
    override val id: String = "DEVICE"
    override val name: String = "Device"
    override val icon: ImageVector = Icons.Filled.Smartphone
    override val middleware: IMiddleware<AppState> = DevicePluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = false

    @Composable
    override fun present(result: WindowResult<DeviceInfo>, onAction: OnAction) {
        val deviceInfo = result.result.firstOrNull()

        if (deviceInfo != null) {
            val cardGroups = deviceInfo.toDisplayItems()

            // Card group indices
            val DEVICE_IDENTITY_INDEX = 0
            val HARDWARE_DISPLAY_INDEX = 1
            val STATUS_INDEX = 2
            val BUILD_INFO_INDEX = 3
            val SYSTEM_PERFORMANCE_INDEX = 4
            val NETWORK_DETAILS_INDEX = 5
            val HARDWARE_FEATURES_INDEX = 6

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top row - 3 cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        listOf(
                            cardGroups[DEVICE_IDENTITY_INDEX] to "Device Identity",
                            cardGroups[HARDWARE_DISPLAY_INDEX] to "Hardware & Display",
                            cardGroups[STATUS_INDEX] to "Status"
                        )
                    ) { (items, title) ->
                        DeviceInfoCard(
                            title = title,
                            items = items,
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }

                // Middle row - 2 cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        listOf(
                            cardGroups[BUILD_INFO_INDEX] to "Build Information",
                            cardGroups[SYSTEM_PERFORMANCE_INDEX] to "System Performance"
                        )
                    ) { (items, title) ->
                        DeviceInfoCard(
                            title = title,
                            items = items,
                            modifier = Modifier.width(420.dp)
                        )
                    }
                }

                // Bottom row - 2 cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        listOf(
                            cardGroups[NETWORK_DETAILS_INDEX] to "Network Details",
                            cardGroups[HARDWARE_FEATURES_INDEX] to "Hardware Features"
                        )
                    ) { (items, title) ->
                        DeviceInfoCard(
                            title = title,
                            items = items,
                            modifier = Modifier.width(420.dp)
                        )
                    }
                }
            }
        }
    }
}
