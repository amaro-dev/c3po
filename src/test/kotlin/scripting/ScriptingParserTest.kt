package scripting

import assertk.assertFailure
import assertk.assertions.isInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class ScriptingParserTest {
    @Test
    fun `Successfully parse the script`() {
        val identifier: InstructionIdentifier = mockk(relaxed = true)
        every { identifier.detect(any()) } returns mockk(relaxed = true)
        val parser = ScriptParser(identifier)
        parser.parse(listOf("", "", ""), Path(""))
    }

    @Test
    fun `Failed to parse the script`() {
        val identifier: InstructionIdentifier = mockk(relaxed = true)
        every { identifier.detect("") } returns mockk(relaxed = true)
        every { identifier.detect("1") } throws UnknownInstructionException("1")
        val parser = ScriptParser(identifier)
        assertFailure {
            parser.parse(listOf("", "", "1"), Path(""))
        }.isInstanceOf(UnknownInstructionException::class)
    }

    @Test
    fun `Failed to validate the script`() {
        val identifier: InstructionIdentifier = mockk(relaxed = true)
        every { identifier.detect("") } returns mockk(relaxed = true) {
            every { validate(any()) } throws ResourceNotFoundException("package.apk", "")
        }
        val parser = ScriptParser(identifier)
        assertFailure {
            parser.parse(listOf("", "", "1"), Path(""))
        }.isInstanceOf(ResourceNotFoundException::class)
    }

}
