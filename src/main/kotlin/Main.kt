import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.Action
import core.App
import core.SettingsState
import ui.*
import java.awt.Toolkit

@Composable
@Preview
fun Render(app: App) {
    val state = app.listen().collectAsState().value

    MaterialTheme {
        Column(Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colors.primary) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(Dimens.ROW_HEIGHT_EXTRA_LARGE.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                            state.currentPlugin
                        ) { app.perform(it) }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                state.currentPlugin?.let { name ->
                    app.plugins.first { it.id == name }.run {
                        present(state.windows) { app.perform(it) }
                    }
                }
                if (state.settingsState == SettingsState.NotFound) {
                    SettingsBox { app.perform(it) }
                }
            }

            Row(
                Modifier.background(MaterialTheme.colors.primaryVariant)
                    .fillMaxWidth()
                    .padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp)
                    .height(Dimens.ROW_HEIGHT_REGULAR.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                CommandStatus(state.commandStatus)
            }
        }
    }
}

fun main() = application {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val myApp = App(clipboard)
    myApp.perform(Action.LoadSettings)
    Window(
        onCloseRequest = ::exitApplication,
        title = "C3PO - The Android Explorer",
        state = WindowState(
            width = Dimens.WINDOW_WIDTH.dp,
            height = Dimens.WINDOW_HEIGHT.dp
        )
    ) {
        Render(myApp)
    }
}
