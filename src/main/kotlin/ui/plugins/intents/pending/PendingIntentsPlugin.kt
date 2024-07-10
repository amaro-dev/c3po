package ui.plugins.intents.pending

import Settings
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import commands.ListPendingActivityIntentsCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.PendingIntent
import ui.Dimens
import ui.MySearchField
import ui.plugins.Plugin
import ui.plugins.packages.PackageHeader

class PendingIntentsPlugin : Plugin<List<Pair<String, List<PendingIntent>>>> {
    sealed interface Action : IAction {
        data object List : Action
    }

    override val id: String = "PENDING_INTENT"
    override val name: String = "Pending intents"
    override val mainAction: IAction = Action.List

    override val middleware: IMiddleware<AppState> = PendingIntentsMiddleware(id)


    override fun isResponsibleFor(action: IAction): Boolean = action is Action

    @Composable
    override fun present(items: List<Pair<String, List<PendingIntent>>>, onAction: (IAction) -> Unit) {
        var filter by remember { mutableStateOf("") }
        val filteredItems = items.filter {
            filter.length < 3 || it.first.contains((filter))
        }
        Box(Modifier.fillMaxSize()) {
            Column {
                MySearchField { filter = it }
                Box {
                    val listState = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize().padding(end = Dimens.SCROLL_BAR_MARGIN.dp), state = listState) {
                        items(filteredItems) { pkg ->
                            PackageHeader(pkg.first)
                            pkg.second.map {
                                PendingIntentRow(it)
                            }

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
}

@Composable
fun PendingIntentRow(intent: PendingIntent) {
    Column {
        Row(Modifier.fillMaxWidth().padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp)) {
            Text(
                text = "Id: ",
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = intent.entryId,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(0.2f)
            )
            Text(
                text = "Type: ",
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = intent.type,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(0.3f)
            )

            Text(
                text = "Flags: ",
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = intent.flags.toString(),
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(0.2f)
            )
            Text(
                text = "Request Code: ",
                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = intent.requestCode?.toString() ?: "<Not specified>",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(0.15f)
            )
        }
        Column(
            Modifier.background(Color.Black.copy(alpha = 0.1f))
                .padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp)
        ) {
            Row {
                Text(
                    text = "Action: ",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = intent.intent.intent.removePrefix("act=").removePrefix("cmp="),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = "Package: ",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = intent.intent.packageName ?: "<Not specified>",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.weight(0.2f)
                )
            }
            Row {
                Text(
                    text = "Component: ",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = intent.intent.cmp ?: "<Not specified>",
                    style = MaterialTheme.typography.body2,
                )
            }
            Row {
                Text(
                    text = "Dat: ",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = intent.intent.dat ?: "<Not specified>",
                    style = MaterialTheme.typography.body2,
                )
            }
            Row {
                Text(
                    text = "Flags: ",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = intent.intent.flags?.let { flags ->
                        IntentFlags.entries
                            .filter { (flags and it.code) > 0 }
                            .joinToString(separator = ", ") { it.name }
                    } ?: "<Not specified>",
                    style = MaterialTheme.typography.body2,
                )
            }
        }

    }
}


class PendingIntentsMiddleware(private val name: String) : IMiddleware<AppState> {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.StartPlugin,
            PendingIntentsPlugin.Action.List -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        val results = ListPendingActivityIntentsCommand(this).run(adbPath)
                        processor.reduce(
                            Action.DeliverPluginResult(name, results.groupBy { it.packageName }.toList())
                        )
                    }
                }
            }
        }
    }
}

enum class IntentFlags(val code: Long) {
    FLAG_ACTIVITY_BROUGHT_TO_FRONT(0x00400000),
    FLAG_ACTIVITY_CLEAR_TASK(0x00008000),
    FLAG_ACTIVITY_CLEAR_TOP(0x04000000),
    FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET(0x00080000),
    FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS(0x00800000),
    FLAG_ACTIVITY_FORWARD_RESULT(0x02000000),
    FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY(0x00100000),
    FLAG_ACTIVITY_LAUNCH_ADJACENT(0x00001000),
    FLAG_ACTIVITY_MATCH_EXTERNAL(0x00000800),
    FLAG_ACTIVITY_MULTIPLE_TASK(0x08000000),
    FLAG_ACTIVITY_NEW_DOCUMENT(0x00080000),
    FLAG_ACTIVITY_NEW_TASK(0x10000000),
    FLAG_ACTIVITY_NO_ANIMATION(0x00010000),
    FLAG_ACTIVITY_NO_HISTORY(0x40000000),
    FLAG_ACTIVITY_NO_USER_ACTION(0x00040000),
    FLAG_ACTIVITY_PREVIOUS_IS_TOP(0x01000000),
    FLAG_ACTIVITY_REORDER_TO_FRONT(0x00020000),
    FLAG_ACTIVITY_RESET_TASK_IF_NEEDED(0x00200000),
    FLAG_ACTIVITY_RETAIN_IN_RECENTS(0x00002000),
    FLAG_ACTIVITY_REQUIRE_DEFAULT(0x00000200),
    FLAG_ACTIVITY_REQUIRE_NON_BROWSER(0x00000400),
    FLAG_ACTIVITY_SINGLE_TOP(0x20000000),
    FLAG_ACTIVITY_TASK_ON_HOME(0x00004000),
    FLAG_DEBUG_LOG_RESOLUTION(0x00000008),
    FLAG_DIRECT_BOOT_AUTO(0x00000100),
    FLAG_EXCLUDE_STOPPED_PACKAGES(0x00000010),
    FLAG_FROM_BACKGROUND(0x00000004),
    FLAG_GRANT_PERSISTABLE_URI_PERMISSION(0x00000040),
    FLAG_GRANT_PREFIX_URI_PERMISSION(0x00000080),
    FLAG_GRANT_READ_URI_PERMISSION(0x00000001),
    FLAG_GRANT_WRITE_URI_PERMISSION(0x00000002),
    FLAG_INCLUDE_STOPPED_PACKAGES(0x00000020),
    FLAG_RECEIVER_FOREGROUND(0x10000000),
    FLAG_RECEIVER_NO_ABORT(0x08000000),
    FLAG_RECEIVER_REGISTERED_ONLY(0x40000000),
    FLAG_RECEIVER_REPLACE_PENDING(0x20000000),
    FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS(0x00200000);
}
