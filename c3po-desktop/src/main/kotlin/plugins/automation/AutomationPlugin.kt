package plugins.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import models.Script
import models.ScriptStep
import models.ScriptStepType
import models.WindowResult
import plugins.Plugin
import ui.OnAction

class AutomationPlugin(
    automationMiddleware: AutomationMiddleware,
) : Plugin<AutomationState> {
    override val id: String = "AUTOMATION"
    override val name: String = "Automation"
    override val middleware: IMiddleware<AppState> = automationMiddleware

    sealed interface Actions : IAction {
        object CreateNewScript : Actions

        data class SetScriptName(
            val name: String,
        ) : Actions

        data class AddStep(
            val stepType: ScriptStepType,
        ) : Actions

        data class RemoveStep(
            val index: Int,
        ) : Actions

        data class MoveStep(
            val fromIndex: Int,
            val toIndex: Int,
        ) : Actions

        data class UpdateStep(
            val index: Int,
            val step: ScriptStep,
        ) : Actions

        object SaveScript : Actions

        object CancelScript : Actions

        object LoadScripts : Actions
    }

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<AutomationState>,
        onAction: OnAction,
    ) {
        val state = result.result.firstOrNull() ?: AutomationState()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header with main actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onAction(Actions.CreateNewScript) },
                ) {
                    Text("Create New Script")
                }

                Button(
                    onClick = { onAction(Actions.LoadScripts) },
                    enabled = !state.isCreatingScript,
                ) {
                    Text("Open Script")
                }
            }

            // Script creation UI
            if (state.isCreatingScript) {
                ScriptCreationUI(state, onAction)
            } else {
                // Script list UI (placeholder for now)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Select 'Create New Script' to start or 'Open Script' to load existing scripts")
                }
            }
        }
    }
}

@Composable
private fun ScriptCreationUI(
    state: AutomationState,
    onAction: OnAction,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Create New Script",
                style = MaterialTheme.typography.h6,
            )

            // Script name input
            var scriptName by remember { mutableStateOf(state.currentScript?.name ?: "") }

            // Update local state when external state changes
            LaunchedEffect(state.currentScript?.name) {
                if (state.currentScript?.name != scriptName) {
                    scriptName = state.currentScript?.name ?: ""
                }
            }

            OutlinedTextField(
                value = scriptName,
                onValueChange = { newValue ->
                    scriptName = newValue
                    onAction(AutomationPlugin.Actions.SetScriptName(newValue))
                },
                label = { Text("Script Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Divider()

            // Steps section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Steps (${state.currentScript?.steps?.size ?: 0})",
                    style = MaterialTheme.typography.subtitle1,
                )

                // Add step dropdown
                AddStepDropdown(onAction)
            }

            // Steps list
            if (state.currentScript?.steps?.isNotEmpty() == true) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(state.currentScript.steps) { index, step ->
                        StepItem(
                            step = step,
                            index = index,
                            onRemove = { onAction(AutomationPlugin.Actions.RemoveStep(index)) },
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No steps added yet. Use 'Add Step' to get started.",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    )
                }
            }

            Divider()

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                OutlinedButton(
                    onClick = { onAction(AutomationPlugin.Actions.CancelScript) },
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onAction(AutomationPlugin.Actions.SaveScript) },
                    enabled = state.currentScript?.name?.isNotBlank() == true,
                ) {
                    Text("Save Script")
                }
            }
        }
    }
}

@Composable
private fun AddStepDropdown(onAction: OnAction) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
        ) {
            Text("Add Step")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ScriptStepType.values().forEach { stepType ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onAction(AutomationPlugin.Actions.AddStep(stepType))
                    },
                ) {
                    Text(stepType.displayName)
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    step: ScriptStep,
    index: Int,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Step number badge
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colors.primary.copy(alpha = 0.1f),
                                CircleShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.subtitle2,
                        color = MaterialTheme.colors.primary,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getStepDisplayName(step),
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface,
                    )

                    val description = getStepDescription(step)
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        )
                    } else {
                        Text(
                            text = "⚠️ Configuration needed",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.error.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp),
            ) {
                Text(
                    text = "×",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

private fun getStepDisplayName(step: ScriptStep): String =
    when (step) {
        is ScriptStep.InstallApk -> "Install APK"
        is ScriptStep.RemovePackage -> "Remove Package"
        is ScriptStep.StartActivity -> "Start Activity"
        is ScriptStep.ClearData -> "Clear Data"
    }

private fun getStepDescription(step: ScriptStep): String =
    when (step) {
        is ScriptStep.InstallApk -> if (step.apkPath.isNotBlank()) "APK: ${step.apkPath}" else ""
        is ScriptStep.RemovePackage -> if (step.packageName.isNotBlank()) "Package: ${step.packageName}" else ""
        is ScriptStep.StartActivity -> {
            if (step.packageName.isNotBlank() && step.activityName.isNotBlank()) {
                "Package: ${step.packageName}, Activity: ${step.activityName}"
            } else {
                ""
            }
        }

        is ScriptStep.ClearData -> if (step.packageName.isNotBlank()) "Package: ${step.packageName}" else ""
    }

data class AutomationState(
    val isCreatingScript: Boolean = false,
    val currentScript: Script? = null,
    val availableScripts: List<String> = emptyList(),
)
