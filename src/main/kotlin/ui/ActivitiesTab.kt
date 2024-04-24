package ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import commands.StartActivityCommand
import models.ActivityInfo
import models.AdbDevice

@Composable
fun ActivitiesTab(device: AdbDevice?, activities: List<ActivityInfo>) {
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
            Box(Modifier.fillMaxSize().padding(10.dp)) {
                val listState = rememberLazyListState()
                LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state = listState) {
                    items(activities.filter {
                        filter.length < 3 || it.packageName.contains((filter))
                    }.groupBy { it.packageName }
                        .flatMap {
                            listOf(Pair(RowType.Header, it.key)).plus(it.value.map { Pair(RowType.Regular, it) })
                        }
                    ) { activity ->
                        if (activity.first == RowType.Header)
                            PackageHeader(activity.second as String)
                        else if (activity.first == RowType.Regular)
                            ActivityRow(activity.second as ActivityInfo, device)

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

enum class RowType {
    Header,
    Regular
}

@Composable
fun PackageHeader(packageName: String) {
    Surface(color = MaterialTheme.colors.primary, shape = MaterialTheme.shapes.small) {
        Box(Modifier.fillMaxWidth().padding(8.dp, 4.dp)) {
            Text(packageName, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun ActivityRow(activity: ActivityInfo, device: AdbDevice?) {
    Box(
        Modifier
            .clickable {
                device?.run {
                    StartActivityCommand(this, activity.fullPath).run()
                }
            }
            .padding(6.dp)
            .fillMaxWidth()
    ) {
        Text(
            activity.activityPath,
            style = MaterialTheme.typography.body2,
        )
    }
}
