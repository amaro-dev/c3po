package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.amaro.sonic.IAction

@Composable
fun RegularRow(content: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp)
            .defaultMinSize(minHeight = Dimens.ROW_HEIGHT_REGULAR.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            content,
            style = MaterialTheme.typography.body2,
        )
    }
}

@Composable
fun ActionableRow(content: String, actions: List<RowAction>, onAction: (IAction) -> Unit) {
    Row(
        Modifier
            .padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(Dimens.HORIZONTAL_SPACER.dp))
        actions.map {
            ActionButton(painterResource(it.icon)) {
                onAction(it.action)
            }
        }
    }
}

data class RowAction(
    val icon: String,
    val description: String,
    val action: IAction
)


@Composable
fun HeaderRow(content: String) {
    Surface(color = MaterialTheme.colors.primary) {
        Row(
            Modifier.fillMaxWidth()
                .padding(Dimens.ROW_HORIZONTAL_MARGIN.dp, Dimens.ROW_VERTICAL_MARGIN.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(content, style = MaterialTheme.typography.subtitle1)
        }
    }
}
