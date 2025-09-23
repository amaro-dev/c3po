package plugins.automation.definition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import plugins.automation.definition.AutomationPlugin
import plugins.automation.structure.ScriptStepType
import ui.OnAction
import ui.component.CustomActionButton

@Composable
fun AddStepDropdownIcon(onAction: OnAction) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        CustomActionButton(
            icon = Icons.Filled.Add,
            contentDescription = "Add Step",
            onClick = { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ScriptStepType.values().forEach { stepType ->
                DropdownMenuItem(
                    text = { Text(stepType.displayName) },
                    onClick = {
                        expanded = false
                        onAction(AutomationPlugin.Actions.AddStep(stepType))
                    }
                )
            }
        }
    }
}

