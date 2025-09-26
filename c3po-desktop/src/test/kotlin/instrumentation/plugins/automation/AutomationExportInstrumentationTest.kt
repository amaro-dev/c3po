package instrumentation.plugins.automation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNull
import core.App
import core.PluginSelectorMiddleware
import core.command.CommandExecutor
import core.model.Action
import core.model.AppReducer
import core.model.AppState
import core.model.AppStateManager
import dev.amaro.sonic.ConditionedDirectMiddleware
import dev.amaro.sonic.IMiddleware
import di.Names
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import plugins.Plugin
import plugins.automation.definition.AutomationPlugin
import plugins.automation.structure.AutomationMiddleware
import plugins.automation.structure.AutomationState
import plugins.automation.structure.Script
import plugins.automation.structure.ScriptPackageService
import plugins.automation.structure.ScriptPackageService.ImportResolution
import plugins.automation.structure.ScriptStorage
import ui.screen.NewLayout
import java.io.File
import java.util.Properties
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile

@OptIn(ExperimentalTestApi::class)
class AutomationExportInstrumentationTest {

    private val scriptStorage = mockk<ScriptStorage>(relaxed = true)
    private val commandExecutor = mockk<CommandExecutor>(relaxed = true)
    private val packageService = mockk<ScriptPackageService>(relaxed = true)

    private lateinit var app: App

    @BeforeEach
    fun setUp() {
        val automationModule = module {
            single { scriptStorage }
            single { commandExecutor }
            single { packageService }
            single { AutomationMiddleware("AUTOMATION", get(), get(), get()) }
            single(named(Names.PLUGIN_LIST_DEPENDENCY)) { listOf<Plugin<*>>(AutomationPlugin(get())) }
            single(named(Names.MIDDLEWARE_LIST_DEPENDENCY)) {
                arrayOf<IMiddleware<AppState>>(
                    PluginSelectorMiddleware(get(named(Names.PLUGIN_LIST_DEPENDENCY))),
                    ConditionedDirectMiddleware(
                        Action.SelectPlugin::class,
                        Action.DeliverPluginResult::class,
                        Action.LoadSettingsIntoState::class,
                        Action.SetSuccess::class,
                        Action.ClearSuccess::class,
                        Action.SetCommandError::class,
                        Action.ClearError::class,
                        Action.SetCommandRunning::class,
                        Action.SetCommandCompleted::class,
                    )
                )
            }
            single {
                AppStateManager(
                    AppState(),
                    AppReducer(),
                    *get<Array<IMiddleware<AppState>>>(named(Names.MIDDLEWARE_LIST_DEPENDENCY)),
                )
            }
        }

        startKoin { modules(automationModule) }
        app = App()
        app.perform(Action.LoadSettingsIntoState(Properties()))
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        stopKoin()
    }

    @Test
    fun `should render automation top bar when plugin state is provided`() = runComposeUiTest {
        val script = createScript()
        val automationState = createAutomationState(
            script = script,
            currentFolder = createTempDirectory("automation-script").toFile().absolutePath
        )

        prepareAutomation(automationState)
        setContent { NewLayout(app) }

        onNodeWithText(script.name).assertIsDisplayed()
    }

    @Test
    fun `should export script to selected directory and show success`() = runComposeUiTest {
        val scriptFolder = createTempDirectory("script-folder").toFile()
        val script = createScript(name = "Demo Script")
        val automationState = createAutomationState(script = script, currentFolder = scriptFolder.absolutePath)
        val destinationDir = createTempDirectory("export-destination").toFile()
        val targetFile = File(destinationDir, "Demo_Script.c3po-script")

        prepareAutomation(automationState)
        setContent { NewLayout(app) }

        every { packageService.resolvePackageFile(script.name, destinationDir) } returns targetFile
        coEvery {
            packageService.exportScript(
                match { it.absolutePath == scriptFolder.absolutePath },
                match { it.absolutePath == destinationDir.absolutePath },
                script.name,
                false
            )
        } returns Result.success(targetFile)

        app.perform(AutomationPlugin.Actions.ExportDestinationChosen(destinationDir.absolutePath))

        val successMessage = "Script '${script.name}' exported to ${targetFile.absolutePath}"
        val state = awaitAppState { appState -> appState.successMessage == successMessage }
        assertThat(state.successMessage).isEqualTo(successMessage)

        coVerify(exactly = 1) {
            packageService.exportScript(
                match { it.absolutePath == scriptFolder.absolutePath },
                match { it.absolutePath == destinationDir.absolutePath },
                script.name,
                false
            )
        }
    }

    @Test
    fun `should show error when script is not saved before export`() = runComposeUiTest {
        val script = createScript(name = "Dirty Script")
        val automationState = createAutomationState(script = script, currentFolder = null, isDirty = true)

        prepareAutomation(automationState)
        setContent { NewLayout(app) }

        app.perform(AutomationPlugin.Actions.ExportScript)

        onNodeWithText("Save the script before exporting").assertIsDisplayed()
        val errorState = awaitAppState { appState -> appState.errorMessage == "Save the script before exporting" }
        assertThat(errorState.errorMessage).isEqualTo("Save the script before exporting")
        coVerify(exactly = 0) {
            packageService.exportScript(
                match { true },
                match { true },
                match { true },
                match { true }
            )
        }
    }

    @Test
    fun `should ask for overwrite when export target exists and proceed on confirm`() = runComposeUiTest {
        val scriptFolder = createTempDirectory("script-folder").toFile()
        val script = createScript(name = "Existing Script")
        val automationState = createAutomationState(script = script, currentFolder = scriptFolder.absolutePath)
        val destinationDir = createTempDirectory("export-overwrite").toFile()
        val targetFile = File(destinationDir, "Existing_Script.c3po-script").apply {
            parentFile?.mkdirs()
            writeText("stub")
        }

        prepareAutomation(automationState)
        setContent { NewLayout(app) }

        every { packageService.resolvePackageFile(script.name, destinationDir) } returns targetFile
        coEvery {
            packageService.exportScript(
                match { it.absolutePath == scriptFolder.absolutePath },
                match { it.absolutePath == destinationDir.absolutePath },
                script.name,
                true
            )
        } returns Result.success(targetFile)

        app.perform(AutomationPlugin.Actions.ExportDestinationChosen(destinationDir.absolutePath))

        val overwriteMessage = "The file ${targetFile.absolutePath} already exists. Do you want to overwrite it?"
        onNodeWithText(overwriteMessage).assertIsDisplayed()

        app.perform(AutomationPlugin.Actions.ConfirmExportOverwrite)

        val successMessage = "Script '${script.name}' exported to ${targetFile.absolutePath}"
        val successState = awaitAppState { appState -> appState.successMessage == successMessage }
        assertThat(successState.successMessage).isEqualTo(successMessage)
        assertTextNotPresent(overwriteMessage)
        val automationAfterExport = successState.automationState() ?: AutomationState()
        assertThat(automationAfterExport.showExportOverwriteDialog).isFalse()

        coVerify(exactly = 1) {
            packageService.exportScript(
                match { it.absolutePath == scriptFolder.absolutePath },
                match { it.absolutePath == destinationDir.absolutePath },
                script.name,
                true
            )
        }
    }

    @Test
    fun `should cancel export when user declines overwrite`() = runComposeUiTest {
        val scriptFolder = createTempDirectory("script-folder").toFile()
        val script = createScript(name = "Cancel Script")
        val automationState = createAutomationState(script = script, currentFolder = scriptFolder.absolutePath)
        val destinationDir = createTempDirectory("export-cancel").toFile()
        val targetFile = File(destinationDir, "Cancel_Script.c3po-script").apply {
            parentFile?.mkdirs()
            writeText("stub")
        }

        prepareAutomation(automationState)
        setContent { NewLayout(app) }

        every { packageService.resolvePackageFile(script.name, destinationDir) } returns targetFile

        app.perform(AutomationPlugin.Actions.ExportDestinationChosen(destinationDir.absolutePath))

        val overwriteMessage = "The file ${targetFile.absolutePath} already exists. Do you want to overwrite it?"
        onNodeWithText(overwriteMessage).assertIsDisplayed()

        app.perform(AutomationPlugin.Actions.CancelExportOverwrite)

        assertTextNotPresent(overwriteMessage)
        coVerify(exactly = 0) {
            packageService.exportScript(
                match { true },
                match { true },
                match { true },
                match { true }
            )
        }
    }

    @Test
    fun `should import script and show success message`() = runComposeUiTest {
        val baseState = createAutomationState(script = null)
        val importFile = createTempFile("import-success", ".c3po-script").toFile()
        val tempDir = createTempDirectory("prepared-import").toFile()
        val rootDir = File(tempDir, "ImportedScript").apply { mkdirs() }
        val importScript = createScript(name = "Imported Script")
        val preparedImport = ScriptPackageService.PreparedImport(
            packageFile = importFile,
            tempDir = tempDir,
            rootDir = rootDir,
            script = importScript
        )
        val scriptsRoot = createTempDirectory("import-target-root").toFile()
        val targetFolder = File(scriptsRoot, "Imported Script")

        prepareAutomation(baseState)
        setContent { NewLayout(app) }

        every { scriptStorage.getScriptFolder(importScript.name) } returns targetFolder
        coEvery { packageService.prepareImport(importFile) } returns Result.success(preparedImport)
        coEvery { packageService.finalizeImport(preparedImport, ImportResolution.Overwrite) } returns Result.success(
            ScriptPackageService.ImportResult(importScript, targetFolder)
        )

        app.perform(AutomationPlugin.Actions.ImportFileChosen(importFile.absolutePath))

        val successMessage = "Script '${importScript.name}' imported successfully"
        val importState = awaitAppState { appState -> appState.successMessage == successMessage }
        assertThat(importState.successMessage).isEqualTo(successMessage)
        onNodeWithText(importScript.name).assertIsDisplayed()

        coVerify(exactly = 1) { packageService.finalizeImport(preparedImport, ImportResolution.Overwrite) }
    }

    @Test
    fun `should allow rename resolution during import conflict`() = runComposeUiTest {
        val baseState = createAutomationState(script = null)
        val importFile = createTempFile("import-conflict", ".c3po-script").toFile()
        val tempDir = createTempDirectory("prepared-import").toFile()
        val rootDir = File(tempDir, "Existing").apply { mkdirs() }
        val conflictingScript = createScript(name = "Existing")
        val preparedImport = ScriptPackageService.PreparedImport(
            packageFile = importFile,
            tempDir = tempDir,
            rootDir = rootDir,
            script = conflictingScript
        )
        val scriptsRoot = createTempDirectory("scripts-root").toFile()
        val existingFolder = File(scriptsRoot, conflictingScript.name).apply { mkdirs() }
        val renamedFolder = File(scriptsRoot, "Existing_copy")

        prepareAutomation(baseState)
        setContent { NewLayout(app) }

        every { scriptStorage.getScriptFolder(conflictingScript.name) } returns existingFolder
        every { scriptStorage.getScriptFolder(match { it.startsWith("Existing_copy") }) } answers {
            File(
                scriptsRoot,
                firstArg()
            )
        }
        coEvery { packageService.prepareImport(importFile) } returns Result.success(preparedImport)
        coEvery {
            packageService.finalizeImport(preparedImport, ImportResolution.Rename("Existing_copy"))
        } returns Result.success(
            ScriptPackageService.ImportResult(conflictingScript.copy(name = "Existing_copy"), renamedFolder)
        )

        app.perform(AutomationPlugin.Actions.ImportFileChosen(importFile.absolutePath))

        onNodeWithText("A script named Existing already exists. Choose overwrite, rename or cancel.").assertIsDisplayed()

        app.perform(AutomationPlugin.Actions.RequestImportRename)
        onNodeWithText("Rename Script").assertIsDisplayed()

        app.perform(AutomationPlugin.Actions.SubmitImportRename("Existing_copy"))

        val successMessage = "Script 'Existing_copy' imported successfully"
        val renamedState = awaitAppState { appState -> appState.successMessage == successMessage }
        assertThat(renamedState.successMessage).isEqualTo(successMessage)
        onNodeWithText("Existing_copy").assertIsDisplayed()

        coVerify(exactly = 1) {
            packageService.finalizeImport(preparedImport, ImportResolution.Rename("Existing_copy"))
        }
    }

    @Test
    fun `should cancel import when user dismisses conflict dialog`() = runComposeUiTest {
        val baseState = createAutomationState(script = null)
        val importFile = createTempFile("import-cancel", ".c3po-script").toFile()
        val tempDir = createTempDirectory("prepared-import").toFile()
        val rootDir = File(tempDir, "Existing").apply { mkdirs() }
        val conflictingScript = createScript(name = "Existing")
        val preparedImport = ScriptPackageService.PreparedImport(
            packageFile = importFile,
            tempDir = tempDir,
            rootDir = rootDir,
            script = conflictingScript
        )
        val scriptsRoot = createTempDirectory("scripts-root").toFile()
        val existingFolder = File(scriptsRoot, conflictingScript.name).apply { mkdirs() }

        prepareAutomation(baseState)
        setContent { NewLayout(app) }

        every { scriptStorage.getScriptFolder(conflictingScript.name) } returns existingFolder
        every { scriptStorage.getScriptFolder(match { it != conflictingScript.name }) } answers {
            File(
                scriptsRoot,
                firstArg()
            )
        }
        coEvery { packageService.prepareImport(importFile) } returns Result.success(preparedImport)

        app.perform(AutomationPlugin.Actions.ImportFileChosen(importFile.absolutePath))

        onNodeWithText("A script named Existing already exists. Choose overwrite, rename or cancel.").assertIsDisplayed()

        app.perform(AutomationPlugin.Actions.CancelImportOverwrite)

        val resolvedImportState = awaitAppState { appState ->
            val automation = appState.automationState()
            automation?.showImportConflictDialog == false
        }
        assertTextNotPresent("A script named Existing already exists. Choose overwrite, rename or cancel.")
        coVerify(exactly = 1) { packageService.discardPreparedImport(preparedImport) }

        val automationState = resolvedImportState.automationState() ?: AutomationState()
        assertThat(automationState.showImportConflictDialog).isFalse()
        assertThat(automationState.pendingImportFilePath).isNull()
    }

    @Test
    fun `should show import error when package preparation fails`() = runComposeUiTest {
        val baseState = createAutomationState(script = null)
        val importFile = createTempFile("import-error", ".c3po-script").toFile()

        prepareAutomation(baseState)
        setContent { NewLayout(app) }

        coEvery { packageService.prepareImport(importFile) } returns Result.failure(RuntimeException("Unsupported script format"))

        app.perform(AutomationPlugin.Actions.ImportFileChosen(importFile.absolutePath))

        val errorMessage = "Failed to import script: Unsupported script format"
        onNodeWithText("Import Error").assertIsDisplayed()
        val errorNodes = onAllNodesWithText(errorMessage).fetchSemanticsNodes()
        assertThat(errorNodes).isNotEmpty()

        val state = awaitAppState { appState -> appState.errorMessage == errorMessage }
        assertThat(state.errorMessage).isEqualTo(errorMessage)
        assertThat(state.automationState()?.importErrorMessage).isEqualTo(errorMessage)

        coVerify(exactly = 0) {
            packageService.finalizeImport(
                match { true },
                match { true }
            )
        }
    }

    private fun prepareAutomation(state: AutomationState) {
        app.perform(Action.DeliverPluginResult(PLUGIN_ID, listOf(state)))
        app.perform(Action.SelectPlugin(PLUGIN_ID))
    }

    private fun ComposeUiTest.assertTextNotPresent(text: String) {
        val nodes = onAllNodesWithText(text).fetchSemanticsNodes()
        assertThat(nodes).isEmpty()
    }

    private fun createAutomationState(
        script: Script? = createScript(),
        currentFolder: String? = null,
        isDirty: Boolean = false,
        isRunning: Boolean = false
    ): AutomationState =
        AutomationState(
            isCreatingScript = true,
            currentScript = script,
            currentScriptFolder = currentFolder,
            isDirty = isDirty,
            isRunning = isRunning
        )

    private fun createScript(name: String = "Demo Script"): Script =
        Script(
            name = name,
            version = "1.0",
            formatVersion = "1.0",
            steps = emptyList()
        )

    private fun awaitAppState(predicate: (AppState) -> Boolean): AppState =
        runBlocking {
            withTimeout(2_000) {
                var result: AppState? = null
                while (result == null) {
                    val state = currentAppState()
                    if (predicate(state)) {
                        result = state
                    } else {
                        delay(20)
                    }
                }
                result!!
            }
        }

    private fun currentAppState(): AppState = app.listen().value

    private fun AppState.automationState(): AutomationState? =
        windows[PLUGIN_ID]?.result?.firstOrNull() as? AutomationState

    private fun currentAutomationState(): AutomationState {
        return currentAppState().automationState() ?: AutomationState()
    }

    private companion object {
        const val PLUGIN_ID = "AUTOMATION"
    }
}
