package ui.rows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.amaro.sonic.IAction
import ui.component.ActionButton
import ui.definitions.Dimens

@Composable
fun ActionableRow(
    actions: List<RowAction>,
    onAction: (IAction) -> Unit,
    content: @Composable () -> Unit,
) {
    BaseRow {
        Box(Modifier.weight(1f)) {
            content()
        }
        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
        actions.map {
            ActionButton(painterResource(it.icon)) {
                onAction(it.action)
            }
        }
    }
}
