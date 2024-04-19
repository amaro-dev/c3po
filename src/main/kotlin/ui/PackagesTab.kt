package ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import models.AdbDevice
import models.AppPackage


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
