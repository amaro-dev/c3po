import assertk.assertThat
import assertk.assertions.isEqualTo
import commands.AdbCommand
import commands.CommandExecutor
import commands.CommandRunner
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import models.AdbDevice
import org.junit.jupiter.api.Test

class CommandExecutorTest {
    @Test
    fun `If everything is well returns the result`() =
        runBlocking {
            val content = "some-content"
            val executor = CommandExecutor()
            val command = FakeCommand("")
            mockkObject(CommandRunner)
            coEvery { CommandRunner.run(any<String>()) } returns Result.success(content)
            assertThat(executor.go(command, "", null)).isEqualTo(content)
        }

    @Test
    fun `If has a device it becomes part of the command`() =
        runBlocking {
            val content = "some-content"
            val instruction = "instruction"
            val device = "device-id"
            val executor = CommandExecutor()
            val command = FakeCommand(instruction)
            mockkObject(CommandRunner)
            coEvery { CommandRunner.run(any<String>()) } returns Result.success(content)
            executor.go(command, "adb", AdbDevice(device))
            val slot = CapturingSlot<String>()
            coVerify { CommandRunner.run(capture(slot)) }
            assertThat(slot.captured).isEqualTo("adb -s $device $instruction")
        }

    @Test
    fun `If it does not have a device the command does not specify one`() =
        runBlocking {
            val content = "some-content"
            val instruction = "instruction"
            val executor = CommandExecutor()
            val command = FakeCommand(instruction)
            mockkObject(CommandRunner)
            coEvery { CommandRunner.run(any<String>()) } returns Result.success(content)
            executor.go(command, "adb", null)
            val slot = CapturingSlot<String>()
            coVerify { CommandRunner.run(capture(slot)) }
            assertThat(slot.captured).isEqualTo("adb $instruction")
        }

    @Test
    fun `Run the command's instruction`() =
        runBlocking {
            val path = "path"
            val cmd = "some-command"
            val executor = CommandExecutor()
            val command = FakeCommand(cmd)
            mockkObject(CommandRunner)
            try {
                // This error is not important for the test
                executor.go(command, path, null)
            } catch (t: Throwable) {
            }
            coVerify { CommandRunner.run("$path $cmd") }
        }
}

class FakeCommand(
    override val command: String,
) : AdbCommand<String> {
    override fun parse(result: String): String = result
}
