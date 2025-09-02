package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.model.Action
import ui.rows.BaseRow

@Composable
fun PluginSelector(
    plugins: List<plugins.Plugin<*>>,
    currentPlugin: String?,
    onSelect: OnAction,
) {
    Column {
        plugins.forEach { plugin ->
            val surfaceColor =
                if (plugin.id == currentPlugin) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            val contentColor =
                if (plugin.id == currentPlugin) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface

            Surface(
                onClick = { onSelect(Action.StartPlugin(plugin.id)) },
                shape = RoundedCornerShape(12.dp),
                color = surfaceColor,
                contentColor = contentColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                BaseRow {
                    Text(plugin.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
