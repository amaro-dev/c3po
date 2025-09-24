import core.command.CommandExecutor
import core.model.Action
import core.model.AppState
import core.model.WindowResult
import dev.amaro.sonic.IProcessor
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import plugins.automation.definition.AutomationPlugin
import plugins.automation.structure.AutomationMiddleware
import plugins.automation.structure.AutomationState
import plugins.automation.structure.Script
import plugins.automation.structure.ScriptPackageService
import plugins.automation.structure.ScriptStorage
import java.io.File
import kotlin.io.path.createTempDirectory

class AutomationMiddlewareTest {

    private val mockScriptStorage = mockk<ScriptStorage>()
    private val mockCommandExecutor = mockk<CommandExecutor>()
    private val mockProcessor = mockk<IProcessor<AppState>>()
    private val mockPackageService = mockk<ScriptPackageService>()

    private val pluginName = "automation"
    private lateinit var middleware: AutomationMiddleware

    private val testScript = Script(
        name = "TestScript",
        version = "1.0",
        formatVersion = "1.0",
        steps = emptyList()
    )

    private val testState = AppState(
        windows = mapOf(
            pluginName to WindowResult(
                searchTerm = "",
                result = listOf(
                    AutomationState(
                        isCreatingScript = true,
                        currentScript = testScript,
                        currentScriptFolder = null
                    )
                )
            )
        )
    )

    private fun tempDir(prefix: String) = createTempDirectory(prefix).toFile().apply { deleteOnExit() }

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { mockProcessor.reduce(any()) } returns Unit
        middleware = AutomationMiddleware(pluginName, mockScriptStorage, mockCommandExecutor, mockPackageService)
    }

    @Test
    fun `SaveScript - keeps script open after saving`() = runBlocking {
        // Given
        val scriptFolderPath = "/path/to/script/folder"
        every { mockScriptStorage.saveScript(testScript) } returns Unit
        every { mockScriptStorage.getScriptFolder("TestScript") } returns File(scriptFolderPath)

        // When
        middleware.asyncProcess(AutomationPlugin.Actions.SaveScript, testState, mockProcessor)

        // Then
        verify {
            mockProcessor.reduce(
                match<Action.DeliverPluginResult> { action ->
                    action.plugin == pluginName &&
                            (action.items.first() as AutomationState).isCreatingScript == true &&    // Script creation UI stays open
                            (action.items.first() as AutomationState).currentScript == testScript &&  // Script is preserved
                            (action.items.first() as AutomationState).currentScriptFolder == scriptFolderPath
                }
            )
        }
        verify { mockScriptStorage.saveScript(testScript) }
    }

    @Test
    fun `SaveScript - should not process when currentScript is null`() = runBlocking {
        // Given
        val stateWithoutScript = testState.copy(
            windows = mapOf(
                pluginName to WindowResult(
                    searchTerm = "",
                    result = listOf(
                        AutomationState(
                            isCreatingScript = true,
                            currentScript = null
                        )
                    )
                )
            )
        )

        // When
        middleware.asyncProcess(AutomationPlugin.Actions.SaveScript, stateWithoutScript, mockProcessor)

        // Then
        verify(exactly = 0) { mockScriptStorage.saveScript(any()) }
        verify(exactly = 0) { mockProcessor.reduce(any()) }
    }

    @Test
    fun `SaveScript - should not process when script name is blank`() = runBlocking {
        // Given
        val scriptWithBlankName = testScript.copy(name = "")
        val stateWithBlankScript = testState.copy(
            windows = mapOf(
                pluginName to WindowResult(
                    searchTerm = "",
                    result = listOf(
                        AutomationState(
                            isCreatingScript = true,
                            currentScript = scriptWithBlankName
                        )
                    )
                )
            )
        )

        // When
        middleware.asyncProcess(AutomationPlugin.Actions.SaveScript, stateWithBlankScript, mockProcessor)

        // Then
        verify(exactly = 0) { mockScriptStorage.saveScript(any()) }
        verify(exactly = 0) { mockProcessor.reduce(any()) }
    }

    @Test
    fun `SaveScript - handles save exception gracefully`() = runBlocking {
        // Given
        every { mockScriptStorage.saveScript(testScript) } throws RuntimeException("Save failed")

        // When
        middleware.asyncProcess(AutomationPlugin.Actions.SaveScript, testState, mockProcessor)

        // Then - Should show error message but not deliver state changes
        verify(exactly = 1) {
            mockProcessor.reduce(
                match<Action.SetCommandError> { action ->
                    action.message == "Failed to save script: Save failed"
                }
            )
        }
        verify { mockScriptStorage.saveScript(testScript) }
    }

    @Test
    fun `CreateNewScript - creates new script in editing state`() = runBlocking {
        // Given
        val initialState = AutomationState()
        val appStateWithInitial = AppState(
            windows = mapOf(
                pluginName to WindowResult(searchTerm = "", result = listOf(initialState))
            )
        )

        // When
        middleware.asyncProcess(AutomationPlugin.Actions.CreateNewScript, appStateWithInitial, mockProcessor)

        // Then
        verify {
            mockProcessor.reduce(
                match<Action.DeliverPluginResult> { action ->
                    action.plugin == pluginName &&
                            (action.items.first() as AutomationState).isCreatingScript == true &&
                            (action.items.first() as AutomationState).currentScript?.name == "" &&
                            (action.items.first() as AutomationState).currentScript?.version == "1.0"
                }
            )
        }
    }

    @Test
    fun `CancelScript - closes script creation and clears currentScript`() = runBlocking {
        // When
        middleware.asyncProcess(AutomationPlugin.Actions.CancelScript, testState, mockProcessor)

        // Then
        verify {
            mockProcessor.reduce(
                match<Action.DeliverPluginResult> { action ->
                    action.plugin == pluginName &&
                            (action.items.first() as AutomationState).isCreatingScript == false &&
                            (action.items.first() as AutomationState).currentScript == null
                }
            )
        }
    }

    @Test
    fun `ExportScript without saved script emits error`() = runBlocking {
        middleware.asyncProcess(AutomationPlugin.Actions.ExportScript, testState, mockProcessor)

        verify {
            mockProcessor.reduce(
                match<Action.SetCommandError> { action ->
                    action.message == "Save the script before exporting"
                }
            )
        }
        coVerify(exactly = 0) { mockPackageService.exportScript(any(), any(), any(), any()) }
    }

    @Test
    fun `ExportDestinationChosen triggers export when destination is available`() = runBlocking {
        val scriptFolderPath = "/path/to/script"
        val destinationDir = tempDir("export-dest")
        val expectedPackage = File(destinationDir, "TestScript.c3po-script")

        val stateWithSavedScript = AppState(
            windows = mapOf(
                pluginName to WindowResult(
                    searchTerm = "",
                    result = listOf(
                        AutomationState(
                            isCreatingScript = true,
                            currentScript = testScript,
                            currentScriptFolder = scriptFolderPath,
                            isDirty = false
                        )
                    )
                )
            )
        )

        every {
            mockPackageService.resolvePackageFile(
                testScript.name,
                match { it.absolutePath == destinationDir.absolutePath }
            )
        } returns expectedPackage

        coEvery {
            mockPackageService.exportScript(
                File(scriptFolderPath),
                match { it.absolutePath == destinationDir.absolutePath },
                testScript.name,
                false
            )
        } returns Result.success(expectedPackage)

        middleware.asyncProcess(
            AutomationPlugin.Actions.ExportDestinationChosen(destinationDir.absolutePath),
            stateWithSavedScript,
            mockProcessor
        )

        coVerify {
            mockPackageService.exportScript(
                File(scriptFolderPath),
                match { it.absolutePath == destinationDir.absolutePath },
                testScript.name,
                false
            )
        }
        verify {
            mockProcessor.reduce(match<Action.SetSuccess> { action ->
                action.message.contains("exported", ignoreCase = true)
            })
        }
    }

    @Test
    fun `ImportFileChosen with existing script shows conflict dialog`() = runBlocking {
        val packagePath = "/tmp/import-package.c3po-script"
        val stagingDir = tempDir("prepared-import")
        val rootDir = File(stagingDir, testScript.name).apply { mkdirs() }
        val prepared = ScriptPackageService.PreparedImport(
            packageFile = File(packagePath),
            tempDir = stagingDir,
            rootDir = rootDir,
            script = testScript
        )

        val existingFolder = tempDir("existing-script")

        coEvery { mockPackageService.prepareImport(File(packagePath)) } returns Result.success(prepared)
        every { mockScriptStorage.getScriptFolder(testScript.name) } returns existingFolder

        middleware.asyncProcess(
            AutomationPlugin.Actions.ImportFileChosen(packagePath),
            testState,
            mockProcessor
        )

        verify {
            mockProcessor.reduce(
                match<Action.DeliverPluginResult> { action ->
                    val newState = action.items.first() as AutomationState
                    newState.showImportConflictDialog &&
                            newState.importConflictExistingName == testScript.name
                }
            )
        }

        coVerify(exactly = 0) { mockPackageService.finalizeImport(any(), any()) }

        prepared.tempDir.deleteRecursively()
    }

    @Test
    fun `ImportFileChosen without conflict finalizes import`() = runBlocking {
        val packagePath = "/tmp/import-package.c3po-script"
        val stagingDir = tempDir("prepared-import")
        val rootDir = File(stagingDir, testScript.name).apply { mkdirs() }
        val prepared = ScriptPackageService.PreparedImport(
            packageFile = File(packagePath),
            tempDir = stagingDir,
            rootDir = rootDir,
            script = testScript
        )

        val targetFolder = File("/scripts/TestScript")

        coEvery { mockPackageService.prepareImport(File(packagePath)) } returns Result.success(prepared)
        every { mockScriptStorage.getScriptFolder(testScript.name) } returns targetFolder
        coEvery {
            mockPackageService.finalizeImport(prepared, ScriptPackageService.ImportResolution.Overwrite)
        } returns Result.success(ScriptPackageService.ImportResult(testScript, targetFolder))

        middleware.asyncProcess(
            AutomationPlugin.Actions.ImportFileChosen(packagePath),
            testState,
            mockProcessor
        )

        coVerify {
            mockPackageService.finalizeImport(prepared, ScriptPackageService.ImportResolution.Overwrite)
        }

        verify {
            mockProcessor.reduce(
                match<Action.DeliverPluginResult> { action ->
                    val newState = action.items.first() as AutomationState
                    newState.currentScript == testScript &&
                            newState.currentScriptFolder == targetFolder.absolutePath
                }
            )
        }

        verify {
            mockProcessor.reduce(match<Action.SetSuccess> { action ->
                action.message.contains("imported successfully", ignoreCase = true)
            })
        }

        stagingDir.deleteRecursively()
    }
}
