import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import commands.*
import core.Action
import core.App
import models.AdbDevice
import models.AppPackage
import ui.PackageRow
import java.io.InputStreamReader

@Composable
@Preview
fun Render(app: App) {
    val state = app.listen().collectAsState().value
    val tabs = listOf("Details", "Packages")

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colors.primary, modifier = Modifier.fillMaxHeight().weight(1f)) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.devices) {
                        Row(Modifier.padding(12.dp, 6.dp)) {
                            RadioButton(
                                selected = it.id == state.currentDevice?.id,
                                onClick = { app.perform(Action.SelectDevice(it)) })
                            Column {
                                Text(it.details.name ?: "<unknown>", style = MaterialTheme.typography.body1)
                                Text(it.id, style = MaterialTheme.typography.overline)
                            }
                        }
                    }
                }
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
                }

            }
        }
    }
}

@Composable
fun DeviceDetailsTab(currentDevice: AdbDevice?) {
    Column(
        Modifier
            .padding(16.dp, 12.dp)
            .fillMaxHeight()
            .border(width = 1.dp, color = MaterialTheme.colors.onBackground)
            .padding(8.dp, 4.dp)
    ) {
        DeviceAttrRow("Name:", currentDevice?.details?.name)
        DeviceAttrRow("Model:", currentDevice?.details?.model)
        DeviceAttrRow("Brand:", currentDevice?.details?.brand)
        DeviceAttrRow("Android SDK:", currentDevice?.details?.sdkLevel)
        DeviceAttrRow("Serial Number:", currentDevice?.details?.serialNumber)

    }
}


@Composable
fun PackagesTab(currentDevice: AdbDevice?, packageList: List<AppPackage>) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(packageList) { pkg ->
            PackageRow(pkg, currentDevice) { cmd -> cmd.run() }
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
fun DeviceAttrRow(label: String, value: String?) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(3f), textAlign = TextAlign.End
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value ?: "-" ,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.weight(7f)
        )
    }
}