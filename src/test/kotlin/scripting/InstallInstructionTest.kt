package scripting

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class InstallInstructionTest {

    @Test
    fun `Install instruction validates the resource`() {
        val instruction = InstallInstruction(listOf("package.apk"))
        instruction.validate(Path("/Users/roarodrigues/dev/projects/c3po/src/test/resources"))
    }

    @Test
    fun `Install instruction is missing arguments`() {
        val instruction = InstallInstruction(emptyList())
        assertFailure {
            instruction.validate(Path("/"))
        }.isInstanceOf(MissingArgumentException::class)
    }

    @Test
    fun `Install instruction saves only file name`() {
        val instruction =
            InstallInstruction(listOf("/Users/roarodrigues/dev/projects/c3po/src/test/resources/package.apk"))
        assertThat(instruction.args).isEqualTo(listOf("package.apk"))
    }

    @Test
    fun `Install instruction cannot find resource`() {
        val instruction = InstallInstruction(listOf("package.apk"))
        assertFailure {
            instruction.validate(Path("/"))
        }.isInstanceOf(ResourceNotFoundException::class)
    }

    @Test
    fun `Install instruction informs its resource`() {
        val instruction =
            InstallInstruction(listOf("/Users/roarodrigues/dev/projects/c3po/src/test/resources/package.apk"))
        assertThat(instruction.resources).isEqualTo(listOf(Path("/Users/roarodrigues/dev/projects/c3po/src/test/resources/package.apk")))
    }
}
