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
                onDismiss = { onAction(Actions.DismissOpenScriptError) },
                primaryAction = DialogAction(
                    text = "OK",
                    onClick = { onAction(Actions.DismissOpenScriptError) },
                    isPrimary = true
                )
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
