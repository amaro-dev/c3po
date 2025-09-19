package core.facade

import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import core.command.SystemCommandExecutor
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.File

class AdbFinderTest {
    @Test
    fun `find returns first validated candidate from PATH`() = runTest {
        val system: SystemCommandExecutor = mockk(relaxed = true)
        val temp = File.createTempFile("adb-mock", null)
        temp.setExecutable(true)
        coEvery { system.executeCommand("command -v adb") } returns Result.success(temp.absolutePath)
        coEvery { system.executeCommand("which -a adb") } returns Result.success("")
        coEvery { system.executeCommand("brew --prefix android-platform-tools") } returns Result.failure(Exception())
        coEvery { system.executeCommand(match { it.endsWith(" version") || it.contains(" version") }) } returns Result.success(
            "Android Debug Bridge version 1.0.41"
        )
        coEvery { system.executeCommand(match { it.startsWith("mdfind ") }) } returns Result.success("")

        val finder = AdbFinder(system)
        val result = finder.find()
        assertThat(result).isSuccess()
    }

    @Test
    fun `find fails with informative message when no candidates`() = runTest {
        val system: SystemCommandExecutor = mockk(relaxed = true)
        coEvery { system.executeCommand(any()) } returns Result.success("")
        val finder = AdbFinder(system)
        val result = finder.find()
        assertThat(result).isFailure()
    }
}
