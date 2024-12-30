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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.Action
import core.App
import core.CompanionState
import ui.AppTheme
import ui.DeviceSelector
import ui.MainScreen
import ui.PluginSelector
import ui.definitions.Dimens
import ui.definitions.Texts
import java.awt.Toolkit


@Composable
fun ServiceStatus(companionState: CompanionState) {
    val (icon, description) = when {
        companionState.isOnline() -> Pair(ui.definitions.Icons.ONLINE, Texts.CONNECTED_TO_COMPANION)
        companionState.isReady() -> Pair(ui.definitions.Icons.CONNECTING, Texts.CONNECTING_TO_COMPANION)
        else -> Pair(ui.definitions.Icons.OFFLINE, Texts.DISCONNECTED_FROM_COMPANION)
    }
    Row {
        Icon(
            painterResource(icon),
            description,
            tint = MaterialTheme.colors.onPrimary,
            modifier = Modifier.size(Dimens.ICON_SIZE_REGULAR.dp)
        )
    }
}


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
                    Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onSurface)) {}
                    Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
                    if (state.currentDevice != null) {
                        PluginSelector(myApp.plugins, state.currentPlugin, onClick)
                    }
                }
            }
        }
    }
