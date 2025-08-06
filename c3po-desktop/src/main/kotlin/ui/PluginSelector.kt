package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import core.Action
import ui.rows.BaseRow

@Composable
fun PluginSelector(
    plugins: List<plugins.Plugin<*>>,
    currentPlugin: String?,
    onSelect: OnAction,
) {
    Column {
        plugins.forEach {
            val surfaceColor =
                if (it.id == currentPlugin) MaterialTheme.colors.primary else MaterialTheme.colors.surface
            val contentColor =
                if (it.id == currentPlugin) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
            Surface(
                color = surfaceColor,
                contentColor = contentColor,
                modifier =
                    Modifier
                        .clickable {
                            onSelect(Action.StartPlugin(it.id))
                        }.fillMaxWidth(),
            ) {
                BaseRow {
                    Text(
                        it.name,
                        style = MaterialTheme.typography.h6,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
