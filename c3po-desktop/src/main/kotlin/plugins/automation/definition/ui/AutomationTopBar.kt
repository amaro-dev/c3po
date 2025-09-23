package plugins.automation.definition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plugins.automation.definition.AutomationPlugin
import plugins.automation.structure.AutomationState
import ui.OnAction
import ui.component.CustomActionButton

@Composable
fun AutomationTopBar(
    state: AutomationState,
    onAction: OnAction,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title + Unsaved chip
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val title = state.currentScript?.name ?: ""
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (state.isDirty) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Unsaved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                // New
                CustomActionButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Create New Script",
                    onClick = { onAction(AutomationPlugin.Actions.OpenNameDialog(isRename = false)) }
                )
                // Open
                CustomActionButton(
                    icon = Icons.Filled.Folder,
                    contentDescription = "Open Script",
                    onClick = { onAction(AutomationPlugin.Actions.OpenScript) },
                )
                // Rename
                val canRename = state.currentScript != null && !state.isRunning
                CustomActionButton(
                    icon = Icons.Filled.Edit,
                    contentDescription = if (canRename) "Rename Script" else "Cannot Rename",
                    onClick = { if (canRename) onAction(AutomationPlugin.Actions.OpenNameDialog(isRename = true)) }
                )
                // Save
                val canSave = state.currentScript?.name?.isNotBlank() == true && !state.isRunning
                CustomActionButton(
                    icon = Icons.Filled.Save,
                    contentDescription = if (canSave) {
                        if (state.isDirty) "Unsaved changes — Save" else "Save Script"
                    } else "Cannot Save",
                    onClick = { if (canSave) onAction(AutomationPlugin.Actions.SaveScript) }
                )
                // Run
                val canRun = state.currentScriptFolder != null && !state.isRunning
                CustomActionButton(
                    icon = Icons.Filled.PlayArrow,
                    contentDescription = if (canRun) "Run Script" else "Cannot Run",
                    onClick = { if (canRun) onAction(AutomationPlugin.Actions.RunScript) }
                )
            }
        }
    }
}

