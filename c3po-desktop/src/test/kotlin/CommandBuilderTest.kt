import assertk.assertThat
import assertk.assertions.containsExactly
import commands.CommandBuilder
import models.AdbDevice
import org.junit.jupiter.api.Test

class CommandBuilderTest {
    @Test
    fun `Build regular command`() {
        val args = CommandBuilder.build(FakeCommand("fake instruction"), "/path/to/adb", AdbDevice("id", "name"))

        assertThat(args).containsExactly(
            "/path/to/adb",
            "-s",
            "id",
            "fake",
            "instruction",
        )
    }

    @Test
    fun `Build command with no device`() {
        val args = CommandBuilder.build(FakeCommand("fake instruction"), "/path/to/adb", null)

        assertThat(args).containsExactly(
            "/path/to/adb",
            "fake",
            "instruction",
        )
    }

    @Test
    fun `Build command with placeholder`() {
        val args =
            CommandBuilder.build(
                FakePlaceholderCommand("/bin/bash¡-c¡[ADB] fake instruction | [ADB]"),
                "/path/to/adb",
                null,
            )

        assertThat(args).containsExactly(
            "/bin/bash",
            "-c",
            "/path/to/adb fake instruction | /path/to/adb",
        )
    }

    @Test
    fun `Build command with placeholder and device`() {
        val args =
            CommandBuilder.build(
                FakePlaceholderCommand("/bin/bash¡-c¡[ADB] fake instruction | [ADB]"),
                "/path/to/adb",
                AdbDevice("id", "name"),
            )

        assertThat(args).containsExactly(
            "/bin/bash",
            "-c",
            "/path/to/adb -s id fake instruction | /path/to/adb -s id",
        )
    }
}
