package ui.plugins.intents.pending

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.PendingIntent
import ui.ContentBox
import ui.Dimens
import ui.Texts
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
        var filter by remember { mutableStateOf(Texts.EMPTY) }
        val filteredItems = items.filter {
            filter.length < 3 || it.first.contains((filter))
        }
        ContentBox({ filter = it }) {
            items(filteredItems) { pkg ->
                PackageHeader(pkg.first)
                pkg.second.map {
                    PendingIntentRow(it)
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
                text = intent.requestCode?.toString() ?: Texts.NOT_SPECIFIED,
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
                    text = intent.intent.packageName ?: Texts.NOT_SPECIFIED,
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
                    text = intent.intent.cmp ?: Texts.NOT_SPECIFIED,
                    style = MaterialTheme.typography.body2,
                )
            }
            Row {
                Text(
                    text = "Dat: ",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = intent.intent.dat ?: Texts.NOT_SPECIFIED,
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
                    } ?: Texts.NOT_SPECIFIED,
                    style = MaterialTheme.typography.body2,
                )
            }
        }

    }
}
