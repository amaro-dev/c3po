package plugins.packages

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import commands.CommandExecutor
import core.Action
import core.Action.CommandAction
import core.AppState
import core.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.AppPackage
import models.SleepState
import plugins.signature.SignatureInfo
import plugins.signature.Signer
import ui.ContentBox
import ui.CopyButton
import ui.OnAction
import ui.baselinePadding
import ui.definitions.Dimens
import ui.definitions.Icons
import ui.definitions.Texts
import ui.horizontalPadding
import ui.onHover
import ui.rows.ActionableRow
import ui.rows.RowAction
import ui.verticalPadding

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
        action is Actions || (action is Action.DeliverSocketResponse &&
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

@Composable
fun SignatureCard(signatureInfo: SignatureInfo, onAction: OnAction) {
    val type = if (signatureInfo.signer.isDebug) androidx.compose.material.icons.Icons.Filled.Warning else
        androidx.compose.material.icons.Icons.Filled.Check
    Surface(color = MaterialTheme.colors.surface) {
        Column(Modifier.fillMaxWidth().horizontalPadding().verticalPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Signature Info", style = MaterialTheme.typography.subtitle1)
                Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
                Icon(type, "")
            }
            Spacer(Modifier.height(Dimens.VERTICAL_SPACER.dp))
            SignatureInfoRow("Name", signatureInfo.signer.commonName ?: "Unspecified")
            SignatureInfoRow("Organization", signatureInfo.signer.organization ?: "Unspecified")
            SignatureInfoRow("Unit", signatureInfo.signer.organizationUnit ?: "Unspecified")
            SignatureInfoRow("MD5", signatureInfo.md5Digest) { onAction(Action.CopyText(it)) }
            SignatureInfoRow("SHA1", signatureInfo.sha1Digest) { onAction(Action.CopyText(it)) }
            SignatureInfoRow("SHA256", signatureInfo.sha256Digest) { onAction(Action.CopyText(it)) }

        }
    }

}

@Composable
fun SignatureInfoRow(label: String, value: String, onCopy: ((String) -> Unit)? = null) {
    var isHoveringValue: Boolean by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.baselinePadding(Dimens.VERTICAL_SPACER)
    ) {
        Text("$label:", style = MaterialTheme.typography.subtitle2)
        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
        Text(
            value,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.weight(1f).onHover { isHoveringValue = it })

        if (onCopy != null) {
            CopyButton(
                isHoveringValue,
                { isHoveringValue = it },
                { onCopy(value) },
            )
        }
    }


}


@Composable
@Preview
fun previewSign() {
    SignatureCard(
        SignatureInfo(
            Signer("commonName", "organizationUnit", "organization", "locality", "state", "CC"),
            "test",
            "test",
            "test"

        ), {}
    )
}
