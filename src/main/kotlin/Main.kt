import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import commands.CommandResult
import core.Action
import core.App
import ui.ActivitiesTab
import ui.DeviceDetailsTab
import ui.DeviceSelector
import ui.PackagesTab
import java.io.InputStreamReader

@Composable
@Preview
fun Render(app: App) {
    val state = app.listen().collectAsState().value
    val tabs = listOf("Details", "Packages", "Activities")

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colors.primary, modifier = Modifier.fillMaxHeight().weight(1f)) {
                DeviceSelector(state.devices)
            }

            Column(Modifier.weight(2f).fillMaxHeight()) {
                Surface(color = MaterialTheme.colors.primaryVariant) {
                    TabRow(selectedTabIndex = state.currentTab) {
                        tabs.forEachIndexed { i, it ->
                            Tab(
                                text = { Text(it) },
                                selected = state.currentTab == i,
                                onClick = { app.perform(Action.SelectTab(i)) }
                            )
                        }
                    }
                }
                when (state.currentTab) {
                    0 -> DeviceDetailsTab(state.currentDevice)
                    1 -> PackagesTab(state.currentDevice, state.packages)
                    2 -> ActivitiesTab(state.currentDevice, state.activities)
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
