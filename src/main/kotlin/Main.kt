import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.Action
import core.App
import core.AppState
import core.CommandStatus
import ui.AppTheme
import ui.CompanionStatus
import ui.DeviceSelector
import ui.MainScreen
import ui.PluginSelector
import ui.RunningAndroid
import ui.definitions.Dimens
import ui.definitions.Texts
import ui.horizontalPadding
import java.awt.Toolkit


fun main() =
    application {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val myApp = App(clipboard)
        myApp.start()
        Window(
            onCloseRequest = {
                myApp.exit()
                exitApplication()
            },
            title = "C3PO - The Android Explorer",
            state =
                WindowState(
                    width = Dimens.WINDOW_WIDTH.dp,
                    height = Dimens.WINDOW_HEIGHT.dp,
                ),
        ) {
            AppTheme {
                MainScreen(
                    myApp
                ) { state, onClick ->
                    Row(Modifier.fillMaxWidth()) {
                        DeviceSelector(
                            state.devices,
                            state.currentDevice,
                            Modifier.weight(1f)
                        ) { onClick(Action.SelectDevice(it)) }
                        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                        IconButton(onClick = { onClick(Action.RefreshDevices) }) {
                            Icon(Icons.Filled.Refresh, Texts.EMPTY)
                        }
                    }
                    Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp - 1.dp))
                    Box(
                        Modifier.fillMaxWidth().height(1.dp)
                            .background(MaterialTheme.colors.onSurface)
                    )
                    Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
                    Row(Modifier.horizontalPadding()) {
                        Box(Modifier.size(Dimens.ICON_SIZE_SMALL.dp), contentAlignment = Alignment.CenterEnd) {
                            CompanionStatus(state.companionState)
                        }
                        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                        Box(Modifier.size(Dimens.ICON_SIZE_SMALL.dp)) {
                            RunningStatus(state)
                        }
                    }
                    Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
                    Box(
                        Modifier.fillMaxWidth().height(1.dp)
                            .background(MaterialTheme.colors.onSurface)
                    )
                    Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
                    if (state.currentDevice != null) {
                        PluginSelector(myApp.plugins, state.currentPlugin, onClick)
                    }

                }
            }
        }
    }


@Composable
fun RunningStatus(state: AppState) {
    if (state.commandStatus == CommandStatus.Running) {
        RunningAndroid()
    }
}
