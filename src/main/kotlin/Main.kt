import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import commands.CommandResult
import core.Action
import core.App
import models.AdbDevice
import models.AppPackage
import models.DeviceAttrs
import ui.PackageRow
import ui.useDebounce
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
                                Text(
                                    it.details[DeviceAttrs.Name.key] ?: "<unknown>",
                                    style = MaterialTheme.typography.body1
                                )
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
    ) {
        DeviceAttrs.entries.forEach {
            DeviceAttrRow("${it.name}:", currentDevice?.details?.run { this[it.key] })
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onBackground))
        }
    }
}


@Composable
fun PackagesTab(currentDevice: AdbDevice?, packageList: List<AppPackage>) {
    var searchTerm by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("") }
    searchTerm.useDebounce {
        filter = it
    }
    Box(Modifier.fillMaxSize().padding(10.dp)) {
        Column {
            OutlinedTextField(
                searchTerm,
                onValueChange = { searchTerm = it },
                leadingIcon = { Icon(Icons.Filled.Search, "") },
                placeholder = { Text("Type to search") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Box {
                val listState = rememberLazyListState()
                LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state = listState) {
                    items(packageList.filter {
                        filter.length < 3 || it.packageName.contains((filter))
                    }) { pkg ->
                        PackageRow(pkg, currentDevice) { cmd -> cmd.run() }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = listState)
                )
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
fun DeviceAttrRow(label: String, value: String?) {
    Row(Modifier.fillMaxWidth().padding(0.dp, 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(3f), textAlign = TextAlign.End
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.weight(7f)
        )
    }
}
