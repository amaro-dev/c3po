import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import commands.CommandResult
import core.Action
import core.App
import dev.amaro.sonic.IAction
import ui.DeviceSelector
import ui.PluginSelector
import ui.plugins.Plugin
import java.io.InputStreamReader

@Composable
@Preview
fun Render(app: App) {
    val state = app.listen().collectAsState().value

    MaterialTheme {
        Column(Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colors.primary) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(3f)) {
                        DeviceSelector(state.devices, state.currentDevice) { app.perform(Action.SelectDevice(it)) }
                    }
                    Spacer(Modifier.width(8.dp))

                    PluginSelector(app.plugins) { app.perform(it) }

                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(8f).fillMaxHeight()) {
                        Workspace(
                            app.plugins.filter { it.name in state.windows.keys },
                            state.currentPlugin
                        ) { app.perform(it) }
                    }
                }
            }

            state.currentPlugin?.let { name ->
                app.plugins.first { it.name == name }.run {
                    present(state.windows) { app.perform(it) }
                }
            }
        }
    }
}

fun main() = application {
    val myApp = App()
    myApp.perform(Action.RefreshDevices)
    Window(onCloseRequest = ::exitApplication, title = "C3PO - The Android Explorer") {
        Render(myApp)
    }
}


object CommandRunner {
    fun run(command: String): CommandResult {
        println("Command: '$command'")
        val process = ProcessBuilder().command(command.split(' ')).start()
        return CommandResult(InputStreamReader(process.inputStream.buffered()).readText().trim())
    }
}

@Composable
fun Workspace(openPlugins: List<Plugin<*>>, currentPlugin: String?, onSelect: (IAction) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        openPlugins.map {
            val color =
                if (currentPlugin == it.name) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.primary
            Box(
                Modifier.weight(1f)
                    .background(color)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clickable { onSelect(Action.SelectPlugin(it.name)) }
            ) {
                Text(it.name, Modifier.fillMaxWidth(1f).padding(4.dp, 8.dp), textAlign = TextAlign.Center)
                Icon(
                    Icons.Filled.Clear, null,
                    modifier = Modifier.align(Alignment.TopEnd).clickable {
                        onSelect(Action.ClosePlugin(it.name))
                    },
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}
