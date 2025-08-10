package plugins.intents.pending

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import commands.CommandExecutor
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.PendingIntent
import models.WindowResult
import ui.ContentBox
import ui.OnAction
import ui.rows.HeaderRow

class PendingIntentsPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<Pair<String, List<PendingIntent>>> {
    sealed interface Actions : IAction {
        data object List : Actions
    }

    override val id: String = "PENDING_INTENT"
    override val name: String = "Pending intents"

    override val middleware: IMiddleware<AppState> = PendingIntentsMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<Pair<String, List<PendingIntent>>>,
        onAction: OnAction,
    ) {
        val items: List<Pair<String, List<PendingIntent>>> = result.result
        val filter = result.searchTerm
        val filteredItems =
            items.filter {
                filter.length < 3 || it.first.contains((filter))
            }
        ContentBox(filter, { onAction(core.Action.ChangeFilter(id, it)) }) {
            items(filteredItems) { pkg ->
                HeaderRow(pkg.first)
                pkg.second.map {
                    PendingIntentRow(it)
                }
            }
        }
    }
}
