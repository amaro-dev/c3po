package core.middleware

import core.facade.update.UpdateChecker
import core.facade.update.UpdateDownloader
import core.facade.update.UpdateInstaller
import core.model.Action
import core.model.AppState
import core.model.UpdateInfo
import dev.amaro.sonic.IProcessor
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.Properties

class UpdateMiddlewareTest {

    private val mockUpdateChecker = mockk<UpdateChecker>()
    private val mockUpdateDownloader = mockk<UpdateDownloader>()
    private val mockUpdateInstaller = mockk<UpdateInstaller>()
    private val mockProcessor = mockk<IProcessor<AppState>>()

    private val middleware = UpdateMiddleware(mockUpdateChecker, mockUpdateDownloader, mockUpdateInstaller)

    private val testState = AppState(
        settings = Properties().apply {
            setProperty("update.auto.enabled", "true")
            setProperty("update.check.url", "https://api.github.com/repos/test/repo/releases/latest")
        },
        appVersion = "2.0.1"
    )

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { mockProcessor.reduce(any()) } returns Unit
    }

    @Test
    fun `asyncProcess - CheckForUpdate calls handleCheckForUpdate`() = runBlocking {
        val action = Action.CheckForUpdate

        every { mockUpdateChecker.isValidUrl(any()) } returns true
        every { mockUpdateChecker.validateVersion(any(), any()) } returns true
        coEvery { mockUpdateChecker.withRetry<UpdateInfo?>(any(), any(), any()) } returns null

        middleware.asyncProcess(action, testState, mockProcessor)

        verify { mockProcessor.reduce(Action.UpdateCheckComplete(null)) }
    }

    @Test
    fun `asyncProcess - DownloadUpdate calls handleDownloadUpdate`() = runBlocking {
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        val action = Action.DownloadUpdate(updateInfo)

        every { mockUpdateChecker.validateVersion("2.1.0", "2.0.1") } returns true
        every { mockUpdateDownloader.downloadUpdate(updateInfo) } returns flowOf(
            UpdateDownloader.DownloadProgress(100, 1024L, 1024L)
        )
        every { mockUpdateDownloader.validateDownloadedFile(any(), any()) } returns
                UpdateDownloader.ValidationResult.Success

        middleware.asyncProcess(action, testState, mockProcessor)

        verify {
            mockProcessor.reduce(Action.UpdateDownloadProgress(100))
            mockProcessor.reduce(match<Action.UpdateDownloadComplete> { it.filePath.contains("c3po-2.1.0-macos.zip") })
        }
    }

    @Test
    fun `asyncProcess - InstallUpdate calls handleInstallUpdate`() = runBlocking {
        val action = Action.InstallUpdate("/path/to/file.dmg")

        coEvery { mockUpdateInstaller.installUpdate(any(), any()) } returns UpdateInstaller.InstallResult.success()

        middleware.asyncProcess(action, testState, mockProcessor)

        verify {
            mockProcessor.reduce(Action.UpdateInstallComplete)
        }
        // Verify automatic restart is no longer triggered
        verify(exactly = 0) { mockProcessor.reduce(Action.RestartApplication) }
    }

    @Test
    fun `asyncProcess - DismissUpdate dispatches directly`() = runBlocking {
        val action = Action.DismissUpdate

        middleware.asyncProcess(action, testState, mockProcessor)

        verify { mockProcessor.reduce(Action.DismissUpdate) }
    }

    @Test
    fun `asyncProcess - CancelDownload calls handleCancelDownload`() = runBlocking {
        val action = Action.CancelDownload

        middleware.asyncProcess(action, testState, mockProcessor)

        verify { mockProcessor.reduce(Action.UpdateCancelled) }
    }

    @Test
    fun `asyncProcess - ignores non-update actions`() = runBlocking {
        val action = Action.ClearError

        middleware.asyncProcess(action, testState, mockProcessor)

        verify(exactly = 0) { mockProcessor.reduce(any()) }
    }

    @Test
    fun `handleCheckForUpdate - auto-updates disabled dispatches error`() = runBlocking {
        val disabledState = testState.copy(
            settings = Properties().apply {
                setProperty("update.auto.enabled", "false")
            }
        )

        middleware.asyncProcess(Action.CheckForUpdate, disabledState, mockProcessor)

        verify { mockProcessor.reduce(match<Action.UpdateError> { it.message == "Auto-updates are disabled" }) }
    }

    @Test
    fun `handleCheckForUpdate - invalid URL dispatches error`() = runBlocking {
        every { mockUpdateChecker.isValidUrl(any()) } returns false

        middleware.asyncProcess(Action.CheckForUpdate, testState, mockProcessor)

        verify { mockProcessor.reduce(match<Action.UpdateError> { it.message.contains("Invalid update URL") }) }
    }

    @Test
    fun `handleCheckForUpdate - network error dispatches error`() = runBlocking {
        every { mockUpdateChecker.isValidUrl(any()) } returns true
        coEvery { mockUpdateChecker.withRetry<UpdateInfo?>(any(), any(), any()) } throws IOException("Network error")

        middleware.asyncProcess(Action.CheckForUpdate, testState, mockProcessor)

        verify { mockProcessor.reduce(match<Action.UpdateError> { it.message.contains("Network error while checking for updates") }) }
    }

    @Test
    fun `handleCheckForUpdate - successful check with newer version`() = runBlocking {
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")

        every { mockUpdateChecker.isValidUrl(any()) } returns true
        every { mockUpdateChecker.validateVersion("2.1.0", "2.0.1") } returns true
        coEvery { mockUpdateChecker.withRetry<UpdateInfo?>(any(), any(), any()) } returns updateInfo

        middleware.asyncProcess(Action.CheckForUpdate, testState, mockProcessor)

        verify { mockProcessor.reduce(Action.UpdateCheckComplete(updateInfo)) }
    }

    @Test
    fun `handleDownloadUpdate - version validation failure dispatches error`() = runBlocking {
        val updateInfo = UpdateInfo("2.0.0", "https://example.com/app.dmg")
        val action = Action.DownloadUpdate(updateInfo)

        every { mockUpdateChecker.validateVersion("2.0.0", "2.0.1") } returns false

        middleware.asyncProcess(action, testState, mockProcessor)

        verify {
            mockProcessor.reduce(match<Action.UpdateError> {
                it.message.contains("Invalid version") && it.message.contains(
                    "not newer"
                )
            })
        }
    }

    @Test
    fun `handleDownloadUpdate - download validation failure dispatches error`() = runBlocking {
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        val action = Action.DownloadUpdate(updateInfo)

        every { mockUpdateChecker.validateVersion("2.1.0", "2.0.1") } returns true
        every { mockUpdateDownloader.downloadUpdate(updateInfo) } returns flowOf(
            UpdateDownloader.DownloadProgress(100, 1024L, 1024L)
        )
        every { mockUpdateDownloader.validateDownloadedFile(any(), any()) } returns
                UpdateDownloader.ValidationResult.Failed("Validation failed")

        middleware.asyncProcess(action, testState, mockProcessor)

        verify {
            mockProcessor.reduce(Action.UpdateDownloadProgress(100))
            mockProcessor.reduce(match<Action.UpdateError> { it.message == "Validation failed" })
        }
    }

    @Test
    fun `handleDownloadUpdate - network error cleans up and dispatches error`() = runBlocking {
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        val action = Action.DownloadUpdate(updateInfo)

        every { mockUpdateChecker.validateVersion("2.1.0", "2.0.1") } returns true
        every { mockUpdateDownloader.downloadUpdate(updateInfo) } throws IOException("Network error")

        middleware.asyncProcess(action, testState, mockProcessor)

        verify { mockProcessor.reduce(match<Action.UpdateError> { it.message.contains("Network error during download") }) }
    }

    @Test
    fun `handleInstallUpdate - installation failure dispatches error`() = runBlocking {
        val action = Action.InstallUpdate("/path/to/file.dmg")

        coEvery { mockUpdateInstaller.installUpdate(any(), any()) } returns
                UpdateInstaller.InstallResult.failure("Installation failed")

        middleware.asyncProcess(action, testState, mockProcessor)

        verify { mockProcessor.reduce(match<Action.UpdateError> { it.message == "Installation failed" }) }
    }

    @Test
    fun `handleInstallUpdate - successful installation without restart`() = runBlocking {
        val action = Action.InstallUpdate("/path/to/file.dmg")

        coEvery { mockUpdateInstaller.installUpdate(any(), any()) } returns
                UpdateInstaller.InstallResult.success()

        middleware.asyncProcess(action, testState, mockProcessor)

        verify { mockProcessor.reduce(Action.UpdateInstallComplete) }
        verify(exactly = 0) { mockProcessor.reduce(Action.RestartApplication) }
    }

    @Test
    fun `handleInstallUpdate - exception dispatches error`() = runBlocking {
        val action = Action.InstallUpdate("/path/to/file.dmg")

        coEvery { mockUpdateInstaller.installUpdate(any(), any()) } throws RuntimeException("Unexpected error")

        middleware.asyncProcess(action, testState, mockProcessor)

        verify {
            mockProcessor.reduce(match<Action.UpdateError> {
                it.message.contains("Installation error") && it.message.contains(
                    "Unexpected error"
                )
            })
        }
    }

    @Test
    fun `handleInstallUpdate - calls progress callback multiple times`() = runBlocking {
        val action = Action.InstallUpdate("/path/to/file.dmg")

        coEvery { mockUpdateInstaller.installUpdate(any(), any()) } coAnswers {
            val onProgress = secondArg<suspend (String) -> Unit>()
            onProgress("Validating installation file...")
            onProgress("Starting installation process...")
            onProgress("Copying files...")
            onProgress("Installation completed successfully")
            UpdateInstaller.InstallResult.success()
        }

        middleware.asyncProcess(action, testState, mockProcessor)

        verify {
            mockProcessor.reduce(Action.UpdateInstallProgress("Validating installation file..."))
            mockProcessor.reduce(Action.UpdateInstallProgress("Starting installation process..."))
            mockProcessor.reduce(Action.UpdateInstallProgress("Copying files..."))
            mockProcessor.reduce(Action.UpdateInstallProgress("Installation completed successfully"))
            mockProcessor.reduce(Action.UpdateInstallComplete)
        }
        // Verify automatic restart is no longer triggered
        verify(exactly = 0) { mockProcessor.reduce(Action.RestartApplication) }
    }

    @Test
    fun `cleanupDownloadedFile - handles file cleanup gracefully`() = runBlocking {
        // Test that cleanup doesn't throw exceptions by running a successful flow
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        val action = Action.DownloadUpdate(updateInfo)

        every { mockUpdateChecker.validateVersion("2.1.0", "2.0.1") } returns true
        every { mockUpdateDownloader.downloadUpdate(updateInfo) } returns flowOf(
            UpdateDownloader.DownloadProgress(100, 1024L, 1024L)
        )
        every { mockUpdateDownloader.validateDownloadedFile(any(), any()) } returns
                UpdateDownloader.ValidationResult.Failed("Test failure")

        // Should not throw exception even if cleanup fails
        middleware.asyncProcess(action, testState, mockProcessor)

        verify { mockProcessor.reduce(match<Action.UpdateError> { it.message == "Test failure" }) }
    }
}
