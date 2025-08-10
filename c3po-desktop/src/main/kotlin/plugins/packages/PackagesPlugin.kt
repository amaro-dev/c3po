package plugins.packages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import commands.CommandExecutor
import core.Action
import core.Action.CommandAction
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.AppPackage
import models.SleepState
import models.WindowResult
import ui.ContentBox
import ui.OnAction
import ui.definitions.Dimens
import ui.definitions.Icons
import ui.definitions.Texts
import ui.rows.ActionableRow
import ui.rows.RowAction

class PackagesPlugin(
    executor: CommandExecutor,
) : plugins.Plugin<AppPackage> {
    companion object {
        const val EXTRACT_KEY_INSTRUCTION = "keystore-info"
        const val CHECK_ASLEEP_INSTRUCTION = "sleep-state"
    }

    sealed interface Actions : IAction {
        data object List : Actions, CommandAction

        data class Stop(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class Uninstall(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class ExtractKey(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class ClearData(
            val packageInfo: AppPackage,
        ) : Actions,
            CommandAction

        data class CheckAsleep(
            val packageInfo: AppPackage,
        ) : Actions
    }

    override val name: String = "Installed packages"

    override val id: String = "PACKAGES"

    override val middleware: IMiddleware<AppState> = PackagesPluginMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean =
        action is Actions ||
                (
                        action is Action.DeliverSocketResponse &&
                                (action.reference.command in arrayOf(EXTRACT_KEY_INSTRUCTION, CHECK_ASLEEP_INSTRUCTION))
                        )

    @Composable
    override fun present(
        result: WindowResult<AppPackage>,
        onAction: OnAction,
    ) {
        val items: List<AppPackage> = result.result
        val filter = result.searchTerm
        ContentBox(filter, { onAction(Action.ChangeFilter(id, it)) }) {
            items(
                items.filter {
                    filter.length < 3 || it.packageName.contains((filter))
                },
            ) { pkg ->
                ActionableRow(
                    listOf(
                        if (pkg.sleepState == SleepState.Unknown) {
                            RowAction(Icons.SLEEPING, Texts.EMPTY, Actions.CheckAsleep(pkg))
                        } else if (pkg.sleepState == SleepState.Awake) {
                            RowAction(Icons.AWAKE, Texts.EMPTY, Action.DoNothing)
                        } else {
                            RowAction(Icons.ASLEEP, Texts.EMPTY, Action.DoNothing)
                        },
                        RowAction(Icons.KEY, Texts.EMPTY, Actions.ExtractKey(pkg)),
                        RowAction(Icons.DELETE, Texts.EMPTY, Actions.Uninstall(pkg)),
                        RowAction(Icons.CLOSE, Texts.EMPTY, Actions.Stop(pkg)),
                        RowAction(Icons.WIPE, Texts.EMPTY, Actions.ClearData(pkg)),
                    ),
                    onAction,
                ) {
                    Column {
                        Text(
                            text = pkg.packageName,
                            style = MaterialTheme.typography.body2,
                        )
                        Text(
                            text = "${pkg.versionName} (${pkg.versionCode})",
                            style = MaterialTheme.typography.overline,
                        )
                    }
                }
                if (pkg.signerInfo != null) {
                    SignatureCard(pkg.signerInfo, onAction)
                }
                Divider(
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier.height(Dimens.BORDER_REGULAR.dp).fillMaxWidth(),
                )
            }
        }
    }
}
