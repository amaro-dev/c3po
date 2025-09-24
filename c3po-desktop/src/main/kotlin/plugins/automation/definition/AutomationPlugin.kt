package plugins.automation.definition


import Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import plugins.Plugin
import plugins.automation.definition.ui.dialogs.ActivitySelectorDialog
import plugins.automation.definition.ui.dialogs.PackageSelectorDialog
import plugins.automation.structure.AutomationMiddleware
import plugins.automation.structure.AutomationState
import plugins.automation.structure.ScriptPackageService
import plugins.automation.structure.ScriptStep
import plugins.automation.structure.ScriptStepType
import ui.OnAction
import ui.component.DialogAction
import ui.component.FilePickerDialog
import ui.component.FilePickerFilter
import ui.component.FilePickerMode
import ui.component.StandardDialog


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

        // Name dialog flow
        data class OpenNameDialog(val isRename: Boolean) : Actions
        object CloseNameDialog : Actions
        data class ConfirmScriptName(val name: String) : Actions

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

        data class ConfigureStopPackage(
            val index: Int,
            val packageName: String,
        ) : Actions

        object SaveScript : Actions
        object RunScript : Actions

        object CancelScript : Actions

        object LoadScripts : Actions

        object ExportScript : Actions
        data class ExportDestinationChosen(val folderPath: String) : Actions
        object CancelExportFlow : Actions
        object ConfirmExportOverwrite : Actions
        object CancelExportOverwrite : Actions

        object ImportScript : Actions
        data class ImportFileChosen(val filePath: String) : Actions
        object CancelImportFlow : Actions
        object ConfirmImportOverwrite : Actions
        object CancelImportOverwrite : Actions
        object RequestImportRename : Actions
        data class SubmitImportRename(val newName: String) : Actions
        object CancelImportRename : Actions
        object DismissImportError : Actions
    }

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(
        result: WindowResult<AutomationState>,
        onAction: OnAction,
    ) {
        val state = result.result.firstOrNull() ?: AutomationState()

        Column(modifier = Modifier.fillMaxSize()) {
            plugins.automation.definition.ui.AutomationTopBar(state = state, onAction = onAction)

            if (state.isCreatingScript) {
                plugins.automation.definition.ui.ScriptCreationScreen(state, onAction)
            } else {
                plugins.automation.definition.ui.EmptyStateCard()
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
                        is ScriptStep.StopPackage -> onAction(Actions.ConfigureStopPackage(stepIndex, packageName))
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
            FilePickerDialog(
                title = "Select APK File",
                mode = FilePickerMode.FILE,
                filter = FilePickerFilter("APK Files", listOf("apk")),
                onFileSelected = { apkPath ->
                    val stepIndex = state.editingStepIndex ?: return@FilePickerDialog
                    onAction(Actions.ConfigureInstallApk(stepIndex, apkPath))
                },
                onDismiss = { onAction(Actions.CancelStepEdit) }
            )
        }

        if (state.showExportPicker) {
            val initialDirectory = state.currentScriptFolder?.let { folder ->
                java.io.File(folder).parentFile?.absolutePath
            } ?: run {
                try {
                    val base =
                        if (Settings.isDebug()) java.io.File(".") else java.io.File(Settings.productionSettingsFolder())
                    java.io.File(base, "scripts").absolutePath
                } catch (_: Exception) {
                    java.io.File("./scripts").absolutePath
                }
            }

            FilePickerDialog(
                title = "Select Destination Folder",
                mode = FilePickerMode.DIRECTORY,
                initialDirectory = initialDirectory,
                onFileSelected = { folder -> onAction(Actions.ExportDestinationChosen(folder)) },
                onDismiss = { onAction(Actions.CancelExportFlow) }
            )
        }

        if (state.showImportPicker) {
            FilePickerDialog(
                title = "Select Script Package",
                mode = FilePickerMode.FILE,
                filter = FilePickerFilter(
                    "C3PO Script Packages",
                    listOf(ScriptPackageService.PACKAGE_EXTENSION)
                ),
                onFileSelected = { path -> onAction(Actions.ImportFileChosen(path)) },
                onDismiss = { onAction(Actions.CancelImportFlow) }
            )
        }

        if (state.showExportOverwriteDialog && state.pendingExportFilePath != null) {
            StandardDialog(
                title = "Overwrite Export File",
                primaryAction = DialogAction(
                    text = "Overwrite",
                    onClick = { onAction(Actions.ConfirmExportOverwrite) },
                    isPrimary = true
                ),
                secondaryAction = DialogAction(
                    text = "Cancel",
                    onClick = { onAction(Actions.CancelExportOverwrite) },
                    isPrimary = false
                ),
                onDismiss = {}
            ) {
                Text("The file ${state.pendingExportFilePath} already exists. Do you want to overwrite it?")
            }
        }

        if (state.showImportConflictDialog && state.importConflictExistingName != null) {
            StandardDialog(
                title = "Script Already Exists",
                primaryAction = DialogAction(
                    text = "Overwrite",
                    onClick = { onAction(Actions.ConfirmImportOverwrite) },
                    isPrimary = true
                ),
                secondaryAction = DialogAction(
                    text = "Rename",
                    onClick = { onAction(Actions.RequestImportRename) },
                    isPrimary = false
                ),
                tertiaryAction = DialogAction(
                    text = "Cancel",
                    onClick = { onAction(Actions.CancelImportOverwrite) },
                    isPrimary = false
                ),
                onDismiss = {}
            ) {
                Text("A script named ${state.importConflictExistingName} already exists. Choose overwrite, rename or cancel.")
            }
        }

        if (state.showImportRenameDialog) {
            plugins.automation.definition.ui.dialogs.ScriptNameDialog(
                isRename = true,
                initialName = state.importSuggestedName ?: "",
                onConfirm = { onAction(Actions.SubmitImportRename(it)) },
                onDismiss = { onAction(Actions.CancelImportRename) }
            )
        }

        state.importErrorMessage?.let { message ->
            StandardDialog(
                title = "Import Error",
                primaryAction = DialogAction(
                    text = "OK",
                    onClick = { onAction(Actions.DismissImportError) },
                    isPrimary = true
                ),
                onDismiss = {}
            ) {
                Text(message)
            }
        }


        // Folder picker for opening a script
        if (state.showOpenScriptPicker) {
            val initialDir = try {
                val base =
                    if (Settings.isDebug()) java.io.File(".") else java.io.File(Settings.productionSettingsFolder())
                java.io.File(base, "scripts").absolutePath
            } catch (_: Exception) {
                java.io.File("./scripts").absolutePath
            }
            FilePickerDialog(
                title = "Select Script Folder",
                mode = FilePickerMode.DIRECTORY,
                initialDirectory = initialDir,
                onFileSelected = { folder -> onAction(Actions.ScriptFolderChosen(folder)) },
                onDismiss = { onAction(Actions.CancelStepEdit) }
            )
        }

        // Error dialog for malformed script
        state.openScriptError?.let { message ->
            StandardDialog(
                title = "Open Script Error",
                primaryAction = DialogAction(
                    text = "OK",
                    onClick = { onAction(Actions.DismissOpenScriptError) },
                    isPrimary = true
                ),
                onDismiss = {}
            ) {
                Text(message)
            }
        }

        // Name dialog for create/rename
        if (state.showNameDialog) {
            plugins.automation.definition.ui.dialogs.ScriptNameDialog(
                isRename = state.isRenameDialog,
                initialName = state.currentScript?.name ?: "",
                onConfirm = { onAction(Actions.ConfirmScriptName(it)) },
                onDismiss = { onAction(Actions.CloseNameDialog) }
            )
        }
    }
}
