package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import core.Action
import dev.amaro.sonic.IAction
import ui.Dimens.Companion.HORIZONTAL_SPACER
import ui.Dimens.Companion.SQUARE_BUTTON_PADDING
import ui.plugins.Plugin

@Composable
fun Workspace(openPlugins: List<Plugin<*>>, currentPlugin: String?, onSelect: (IAction) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        openPlugins.map {
            val color =
                if (currentPlugin == it.id) MaterialTheme.colors.primary else MaterialTheme.colors.primaryVariant
            Box(
                Modifier.weight(1f)
                    .background(color)
                    .fillMaxHeight()
                    .clickable { onSelect(Action.SelectPlugin(it.id)) }
                    .padding(SQUARE_BUTTON_PADDING.dp)
            ) {
                Text(
                    it.name,
                    Modifier.fillMaxWidth(1f)
                        .padding(HORIZONTAL_SPACER.dp),
                    textAlign = TextAlign.Center
                )
                Icon(
                    Icons.Filled.Clear, null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable {
                            onSelect(Action.ClosePlugin(it.id))
                        },
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}
