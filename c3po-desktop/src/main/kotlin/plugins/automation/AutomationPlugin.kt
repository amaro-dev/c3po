package plugins.automation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        data class EditStep(
            val index: Int,
        ) : Actions

        object CancelStepEdit : Actions

        data class ConfigureInstallApk(
            val index: Int,
            val apkPath: String,
        ) : Actions

        data class ConfigureRemovePackage(
            val index: Int,
            val packageName: String,
        ) : Actions

        data class ConfigureStartActivity(
            val index: Int,
            val packageName: String,
            val activityName: String,
        ) : Actions

        data class ConfigureClearData(
            val index: Int,
            val packageName: String,
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

        // Configuration Dialogs
        if (state.showPackageSelector) {
            PackageSelectorDialog(
                packages = state.availablePackages,
                onPackageSelected = { packageName ->
                    val stepIndex = state.editingStepIndex ?: return@PackageSelectorDialog
                    val currentStep = state.currentScript?.steps?.getOrNull(stepIndex) ?: return@PackageSelectorDialog

                    when (currentStep) {
                        is ScriptStep.RemovePackage -> onAction(Actions.ConfigureRemovePackage(stepIndex, packageName))
                        is ScriptStep.ClearData -> onAction(Actions.ConfigureClearData(stepIndex, packageName))
                        else -> onAction(Actions.CancelStepEdit)
                    }
                },
                onDismiss = { onAction(Actions.CancelStepEdit) }
            )
        }

        if (state.showActivitySelector) {
            val stepIndex = state.editingStepIndex ?: 0

            ActivitySelectorDialog(
                activities = state.availableActivities,
                selectedPackage = "", // No pre-selected package
                onActivitySelected = { activityPath ->
                    // Extract package name from the selected activity
                    val selectedActivity = state.availableActivities.find { it.activityPath == activityPath }
                    if (selectedActivity != null) {
                        onAction(
                            Actions.ConfigureStartActivity(
                                stepIndex,
                                selectedActivity.packageName,
                                selectedActivity.activityPath
                            )
                        )
                    }
                },
                onDismiss = { onAction(Actions.CancelStepEdit) }
            )
        }

        if (state.showApkPicker) {
            ApkPickerDialog(
                onApkSelected = { apkPath ->
                    val stepIndex = state.editingStepIndex ?: return@ApkPickerDialog
                    onAction(Actions.ConfigureInstallApk(stepIndex, apkPath))
                },
                onDismiss = { onAction(Actions.CancelStepEdit) }
            )
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
                            onEdit = {
                                // Only allow editing if no dialogs are currently open
                                if (!state.showPackageSelector && !state.showActivitySelector && !state.showApkPicker) {
                                    onAction(AutomationPlugin.Actions.EditStep(index))
                                }
                            },
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
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
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

@Composable
private fun PackageSelectorDialog(
    packages: List<models.AppPackage>,
    onPackageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchText by remember { mutableStateOf("") }
    val filteredPackages = remember(packages, searchText) {
        if (searchText.isBlank()) {
            packages
        } else {
            packages.filter { it.packageName.contains(searchText, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Package") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search packages...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredPackages.size) { index ->
                        val packageInfo = filteredPackages[index]
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onPackageSelected(packageInfo.packageName)
                            },
                            elevation = 1.dp,
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = packageInfo.packageName,
                                    style = MaterialTheme.typography.subtitle2,
                                )
                                if (packageInfo.versionName.isNotBlank()) {
                                    Text(
                                        text = "Version: ${packageInfo.versionName}",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ActivitySelectorDialog(
    activities: List<models.ActivityInfo>,
    selectedPackage: String,
    onActivitySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchText by remember { mutableStateOf("") }
    val filteredActivities = remember(activities, searchText) {
        if (searchText.isBlank()) {
            activities
        } else {
            activities.filter { it.activityPath.contains(searchText, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Activity")
                if (selectedPackage.isNotBlank()) {
                    Text(
                        text = "Package: $selectedPackage",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search activities...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredActivities.size) { index ->
                        val activity = filteredActivities[index]
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onActivitySelected(activity.activityPath)
                            },
                            elevation = 1.dp,
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = activity.activityPath,
                                    style = MaterialTheme.typography.subtitle2,
                                )
                                Text(
                                    text = "Package: ${activity.packageName}",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ApkPickerDialog(
    onApkSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var filePath by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select APK File") },
        text = {
            Column {
                OutlinedTextField(
                    value = filePath,
                    onValueChange = { filePath = it },
                    label = { Text("APK file path") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("/path/to/your/app.apk") },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter the full path to your APK file. The file will be copied to the script folder.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (filePath.isNotBlank()) {
                        onApkSelected(filePath)
                    }
                },
                enabled = filePath.isNotBlank()
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
    val editingStepIndex: Int? = null,
    val availablePackages: List<models.AppPackage> = emptyList(),
    val availableActivities: List<models.ActivityInfo> = emptyList(),
    val showPackageSelector: Boolean = false,
    val showActivitySelector: Boolean = false,
    val showApkPicker: Boolean = false,
)
