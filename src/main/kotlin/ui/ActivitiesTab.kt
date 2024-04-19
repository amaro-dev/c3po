package ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import commands.StartActivityCommand
import models.AdbDevice

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivitiesTab(device: AdbDevice?, activities: List<String>) {
    Box {
        val listState = rememberLazyListState()
        LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state = listState) {
            items(activities) { activity ->
                Text(
                    activity,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(2.dp)
                        .onClick {
                            device?.run {
                                StartActivityCommand(this, activity).run()
                            }
                        }
                )
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState = listState)
        )
    }
}
