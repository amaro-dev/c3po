package scripting

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import org.junit.jupiter.api.Test

class InstructionIdentifierTest {
    @Test
    fun `Successfully identify the uninstall instruction`() {
        val identifier = InstructionIdentifierImpl()
        val instruction = identifier.detect("uninstall br.com.amaro.c3po")
        assertThat(instruction).all {
            isInstanceOf(UninstallInstruction::class)
            prop(Instruction::args).isEqualTo(listOf("br.com.amaro.c3po"))
        }
    }

    @Test
    fun `Successfully identify the install instruction`() {
        val identifier = InstructionIdentifierImpl()
        val instruction = identifier.detect("install package.apk")
        assertThat(instruction).all {
            isInstanceOf(InstallInstruction::class)
            prop(Instruction::args).isEqualTo(listOf("package.apk"))
        }
    }

    @Test
    fun `Successfully identify the clear instruction`() {
        val identifier = InstructionIdentifierImpl()
        val instruction = identifier.detect("clear br.com.amaro.c3po")
        assertThat(instruction).all {
            isInstanceOf(ClearInstruction::class)
            prop(Instruction::args).isEqualTo(listOf("br.com.amaro.c3po"))
        }
    }

    @Test
    fun `Unknown instruction received results in exception`() {
        val identifier = InstructionIdentifierImpl()
        assertFailure {
            identifier.detect("unknown br.com.amaro.c3po")
        }.isInstanceOf(UnknownInstructionException::class)
            .prop(Exception::message).isEqualTo("Could not parse 'unknown' instruction.")
    }
}
