import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.Action
import core.App
import core.CompanionState
import core.SettingsState
import kotlinx.coroutines.delay
import ui.AppTheme
import ui.CommandStatus
import ui.DeviceSelector
import ui.Dimens
import ui.PluginSelector
import ui.SettingsBox
import ui.Texts
import ui.Workspace
import ui.plugins.Plugin
import java.awt.Toolkit

@Composable
@Preview
fun Render(app: App) {
    val state = app.listen().collectAsState().value
    AppTheme {
        Column(Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colors.primary) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(Dimens.ROW_HEIGHT_EXTRA_LARGE.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(3f)) {
                        DeviceSelector(state.devices, state.currentDevice) { app.perform(Action.SelectDevice(it)) }
                    }
                    Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                    IconButton(onClick = { app.perform(Action.RefreshDevices) }) {
                        Icon(Icons.Filled.Refresh, Texts.EMPTY)
                    }
                    Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                    PluginSelector(app.plugins) { app.perform(it) }
                    Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                    Box(Modifier.weight(8f).fillMaxHeight()) {
                        Workspace(
                            app.plugins.filter { it.id in state.windows.keys },
                            state.currentPlugin,
                        ) { app.perform(it) }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f).background(color = MaterialTheme.colors.background)) {
                state.currentPlugin?.let { name ->
                    val selectedPlugin: Plugin<*>? = app.plugins.find { it.id == name }
                    selectedPlugin?.render(state.windows) { app.perform(it) }
                }
                if (state.settingsState == SettingsState.NotFound) {
                    SettingsBox { app.perform(it) }
                }
            }
            Row(
                Modifier
                    .background(MaterialTheme.colors.primary)
                    .fillMaxWidth()
                    .padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp)
                    .height(Dimens.ROW_HEIGHT_REGULAR.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Row(Modifier.weight(1f)) {
                    AnimatedVisibility(state.errorMessage != null) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colors.onPrimary,
                        )
                    }
                }
                LaunchedEffect(state.commandStatus) {
                    delay(3000)
                    if (state.commandStatus != core.CommandStatus.Idle) {
                        app.perform(Action.ClearError)
                    }
                }
                CommandStatus(state.commandStatus)
                Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                ServiceStatus(state.companionState)
            }
        }
    }
}

@Composable
fun ServiceStatus(companionState: CompanionState) {
    val (icon, description) =
        when {
            companionState.isReady() -> Pair(ui.Icons.CONNECTING, Texts.CONNECTING_TO_COMPANION)
            companionState.isOnline() -> Pair(ui.Icons.ONLINE, Texts.CONNECTED_TO_COMPANION)
            else -> Pair(ui.Icons.OFFLINE, Texts.DISCONNECTED_FROM_COMPANION)
        }
    Row {
        Icon(
            painterResource(icon),
            description,
            tint = MaterialTheme.colors.onPrimary,
            modifier = Modifier.size(Dimens.ICON_SIZE_REGULAR.dp),
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
            Render(myApp)
        }
    }

@Composable
fun CompanionDialog() {
    // .clickable { onAction(Action.PrepareCompanion) }
}
