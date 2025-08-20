package plugins.automation.definition

import Settings
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.Plugin
import plugins.automation.structure.AutomationMiddleware
import plugins.automation.structure.AutomationState
import plugins.automation.structure.Script
import plugins.automation.structure.ScriptStep
import plugins.automation.structure.ScriptStepType
import ui.OnAction
import ui.component.CustomActionButton
import ui.component.CustomTextField
import ui.component.EnhancedHeaderRow

class AutomationPlugin(
    automationMiddleware: AutomationMiddleware,
) : Plugin<AutomationState> {
    override val id: String = "AUTOMATION"
    override val name: String = "Automation"
    override val icon: ImageVector = Icons.Filled.PlayCircle
    override val middleware: IMiddleware<AppState> = automationMiddleware

    sealed interface Actions : IAction {
        object CreateNewScript : Actions
        object OpenScript : Actions
        data class ScriptFolderChosen(val folderPath: String) : Actions
        object DismissOpenScriptError : Actions

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
        object RunScript : Actions

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

        Column(modifier = Modifier.fillMaxSize()) {
            // Header card with main actions
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

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
                            onClick = { onAction(Actions.OpenScript) },
                            enabled = !state.isCreatingScript,
                        ) {
                            Text("Open Script")
                        }

                        // Save button - only show when creating script
                        if (state.isCreatingScript) {
                            Button(
                                onClick = { onAction(Actions.SaveScript) },
                                enabled = state.currentScript?.name?.isNotBlank() == true && !state.isRunning,
                            ) {
                                Text("Save Script")
                            }
                        }
                    }
                }
            }

            // Script creation UI or empty state
            if (state.isCreatingScript) {
                ScriptCreationUI(state, onAction)
            } else {
                // Empty state card
                Card(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No script in progress",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select 'Create New Script' to start or 'Open Script' to load existing scripts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
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

        // Minimal run logs (if running)
        if (state.isRunning && state.runLogs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EnhancedHeaderRow("Run Logs")
                    Spacer(modifier = Modifier.height(8.dp))
                    state.runLogs.takeLast(50).forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Native folder picker for opening a script (MVP)
        if (state.showOpenScriptPicker) {
            val initialDir = try {
                val base =
                    if (Settings.isDebug()) java.io.File(".") else java.io.File(Settings.productionSettingsFolder())
                java.io.File(base, "scripts").absolutePath
            } catch (_: Exception) {
                java.io.File("./scripts").absolutePath
            }
            NativeFolderPicker(
                initialDirectory = initialDir,
                onFolderChosen = { folder -> onAction(Actions.ScriptFolderChosen(folder)) },
                onDismiss = { onAction(Actions.CancelStepEdit) }
            )
        }

        // Error dialog for malformed script
        state.openScriptError?.let { message ->
            AlertDialog(
                onDismissRequest = { onAction(Actions.DismissOpenScriptError) },
                title = { Text("Open Script Error") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(
                        onClick = { onAction(Actions.DismissOpenScriptError) }
                    ) { Text("OK") }
                }
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    EnhancedHeaderRow("Create New Script")
                }

                item {
                    // Script name input
                    var scriptName by remember { mutableStateOf(state.currentScript?.name ?: "") }

                    // Update local state when external state changes
                    LaunchedEffect(state.currentScript?.name) {
                        if (state.currentScript?.name != scriptName) {
                            scriptName = state.currentScript?.name ?: ""
                        }
                    }

                    CustomTextField(
                        value = scriptName,
                        onValueChange = { newValue ->
                            scriptName = newValue
                            onAction(AutomationPlugin.Actions.SetScriptName(newValue))
                        },
                        placeholder = "Enter script name...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Divider(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Steps section header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Steps (${state.currentScript?.steps?.size ?: 0})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Add step dropdown (disabled while running)
                            if (!state.isRunning) {
                                AddStepDropdown(onAction)
                            } else {
                                Text(
                                    text = "Running...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }

                            // Run button next to Add Step
                            Button(
                                onClick = { onAction(AutomationPlugin.Actions.RunScript) },
                                enabled = state.currentScriptFolder != null && !state.isRunning,
                            ) {
                                Text("Run")
                            }
                        }
                    }
                }

                // Steps list
                if (state.currentScript?.steps?.isNotEmpty() == true) {
                    itemsIndexed(state.currentScript.steps) { index, step ->
                        StepItem(
                            step = step,
                            index = index,
                            onRemove = { onAction(AutomationPlugin.Actions.RemoveStep(index)) },
                            onEdit = {
                                // Only allow editing if not running and no dialogs are open
                                if (!state.isRunning && !state.showPackageSelector && !state.showActivitySelector && !state.showApkPicker) {
                                    onAction(AutomationPlugin.Actions.EditStep(index))
                                }
                            },
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No steps added yet. Use 'Add Step' to get started.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                item {
                    Divider(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
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
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = listState)
            )
        }
    }
}

@Composable
private fun AddStepDropdown(onAction: OnAction) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true }
        ) {
            Text("Add Step")
        }

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

@Composable
private fun StepItem(
    step: ScriptStep,
    index: Int,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getStepDisplayName(step),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    val description = getStepDescription(step)
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    } else {
                        Text(
                            text = "⚠️ Configuration needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            CustomActionButton(
                icon = Icons.Filled.Close,
                contentDescription = "Remove step",
                onClick = onRemove
            )
        }
    }
}

@Composable
private fun PackageSelectorDialog(
    packages: List<core.model.AppPackage>,
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
                CustomTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = "Search packages...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredPackages.size) { index ->
                        val packageInfo = filteredPackages[index]
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onPackageSelected(packageInfo.packageName)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = packageInfo.packageName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (packageInfo.versionName.isNotBlank()) {
                                    Text(
                                        text = "Version: ${packageInfo.versionName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
    activities: List<core.model.ActivityInfo>,
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        text = {
            Column {
                CustomTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = "Search activities...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredActivities.size) { index ->
                        val activity = filteredActivities[index]
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onActivitySelected(activity.activityPath)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = activity.activityPath,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Package: ${activity.packageName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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

    // File picker launcher
    LaunchedEffect(Unit) {
        try {
            val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Select APK File", java.awt.FileDialog.LOAD)
            fileDialog.setFilenameFilter { _, name ->
                name.lowercase().endsWith(".apk")
            }
            fileDialog.isVisible = true

            val selectedFile = fileDialog.file
            val selectedDir = fileDialog.directory

            if (selectedFile != null && selectedDir != null) {
                filePath = "$selectedDir$selectedFile"
            } else {
                // User cancelled the dialog
                onDismiss()
            }
        } catch (e: Exception) {
            // Fallback to manual input if file dialog fails
            filePath = ""
        }
    }

    if (filePath.isNotEmpty()) {
        // Show confirmation dialog with selected file
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirm APK Selection") },
            text = {
                Column {
                    Text(
                        text = "Selected APK file:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = filePath,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "The file will be copied to the script folder.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onApkSelected(filePath) }
                ) {
                    Text("Use This File")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
    } else {
        // Fallback manual input dialog
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select APK File") },
            text = {
                Column {
                    Text(
                        text = "File picker unavailable. Please enter the path manually:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CustomTextField(
                        value = filePath,
                        onValueChange = { filePath = it },
                        placeholder = "/path/to/your/app.apk",
                        modifier = Modifier.fillMaxWidth()
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
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NativeFolderPicker(
    initialDirectory: String,
    onFolderChosen: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(Unit) {
        try {
            // Temporarily enable directory selection on macOS
            val key = "apple.awt.fileDialogForDirectories"
            val previous = try {
                System.getProperty(key)
            } catch (_: Exception) {
                null
            }
            try {
                try {
                    System.setProperty(key, "true")
                } catch (_: Exception) {
                }

                val chooser =
                    java.awt.FileDialog(null as java.awt.Frame?, "Select Script Folder", java.awt.FileDialog.LOAD)
                chooser.isMultipleMode = false
                chooser.directory = initialDirectory
                chooser.isVisible = true

                val selectedPath = if (chooser.file != null) {
                    java.io.File(chooser.directory, chooser.file).absolutePath
                } else {
                    null
                }

                if (selectedPath != null) onFolderChosen(selectedPath) else onDismiss()
            } finally {
                try {
                    if (previous == null) System.clearProperty(key) else System.setProperty(key, previous)
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            onDismiss()
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
    val editingStepIndex: Int? = null,
    val availablePackages: List<core.model.AppPackage> = emptyList(),
    val availableActivities: List<core.model.ActivityInfo> = emptyList(),
    val showPackageSelector: Boolean = false,
    val showActivitySelector: Boolean = false,
    val showApkPicker: Boolean = false,
)
