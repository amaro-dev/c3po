import assertk.assertThat
import assertk.assertions.isEqualTo
import commands.AdbCommand
import commands.CommandBuilder
import commands.CommandExecutor
import commands.CommandRunner
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import models.AdbDevice
import org.junit.jupiter.api.Test
import java.io.File

class CommandExecutorTest {
    @Test
    fun `If everything is well returns the result`() =
        runBlocking {
            val response = mockk<AdbDevice>(relaxed = true)
            val command = mockk<AdbCommand<AdbDevice>>(relaxed = true) {
                every { parse("") } returns response
            }
            mockkObject(CommandRunner)
            coEvery { CommandRunner.run(any<File>(), any()) } returns Result.success("")
            val executor = CommandExecutor()
            assertThat(executor.go(command, "", null)).isEqualTo(Result.success(response))
            clearMocks(CommandRunner)
        }

    @Test
    fun `If it fails returns de exception`() =
        runBlocking {
            val response = mockk<AdbDevice>(relaxed = true)
            val command = mockk<AdbCommand<AdbDevice>>(relaxed = true) {
                every { parse("") } returns response
            }
            val exception = Exception("Failure")
            mockkObject(CommandRunner)
            coEvery { CommandRunner.run(any<File>(), any()) } returns Result.failure(exception)
            val executor = CommandExecutor()
            assertThat(executor.go(command, "", null)).isEqualTo(Result.failure(exception))
            clearMocks(CommandRunner)
        }

    @Test
    fun `Calls CommandRunner with the folder where adb is located`() = runBlocking {
        val response = mockk<AdbDevice>(relaxed = true)
        val command = mockk<AdbCommand<AdbDevice>>(relaxed = true) {
            every { parse("") } returns response
        }
        val exception = Exception("Failure")
        mockkObject(CommandRunner)
        coEvery { CommandRunner.run(any<File>(), any()) } returns Result.failure(exception)
        CommandExecutor().go(command, "/path/to/adb", null)
        coVerify { CommandRunner.run(File("/path/to"), any()) }
        clearMocks(CommandRunner)
    }

    @Test
    fun `Calls CommandRunner with the built params from CommandBuilder`() = runBlocking {
        val response = mockk<AdbDevice>(relaxed = true)
        val command = mockk<AdbCommand<AdbDevice>>(relaxed = true) {
            every { parse("") } returns response
        }
        val exception = Exception("Failure")
        mockkObject(CommandRunner)
        coEvery { CommandRunner.run(any<File>(), any()) } returns Result.failure(exception)
        mockkObject(CommandBuilder)
        every { CommandBuilder.build(any(), any(), any()) } returns arrayOf("param1", "param2", "param3")

        CommandExecutor().go(command, "/path/to/adb", null)
        coVerify { CommandRunner.run(any(), arrayOf("param1", "param2", "param3")) }
        clearMocks(CommandRunner, CommandBuilder)
    }


}
