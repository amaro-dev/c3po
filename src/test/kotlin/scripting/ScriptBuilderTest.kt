package scripting

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.exists
import assertk.assertions.hasText
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import java.io.File
import java.util.zip.ZipFile
import kotlin.io.path.Path

class ScriptBuilderTest {
    companion object {
        private const val TEMP_FOLDER = "/Users/roarodrigues/dev/projects/c3po/build/tmp"
        private const val SAMPLE_FILE_NAME = "package.apk"
        private const val SAMPLE_FILE = "/Users/roarodrigues/dev/projects/c3po/src/test/resources/$SAMPLE_FILE_NAME"
    }

    @Test
    fun `Create new instance with single instruction and save`() {
        val scriptName = "script-name"
        val instance = ScriptBuilder(scriptName, Path(TEMP_FOLDER))
        instance.append(ClearInstruction(listOf("br.com.amaro.c3po")))
        instance.pack()
        assertThat(File(TEMP_FOLDER, "$scriptName.sc3po")).all {
            exists()
            hasText("clear br.com.amaro.c3po\n")
        }
    }

    @Test
    fun `Create new instance with two instruction and save`() {
        val scriptName = "script-name"
        val instance = ScriptBuilder(scriptName, Path(TEMP_FOLDER))
        instance.append(InstallInstruction(listOf(SAMPLE_FILE)))
        instance.append(ClearInstruction(listOf("br.com.amaro.c3po")))
        instance.pack()
        assertThat(File(TEMP_FOLDER, "$scriptName.sc3po")).all {
            exists()
            hasText("install package.apk\nclear br.com.amaro.c3po\n")
        }
    }

    @Test
    fun `When packing it creates a zip file containing the script and resources`() {
        val scriptName = "script-name"
        val instance = ScriptBuilder(scriptName, Path(TEMP_FOLDER))
        removeFileIfExist("$scriptName.zip")
        instance.append(InstallInstruction(listOf(SAMPLE_FILE)))
        instance.append(ClearInstruction(listOf("br.com.amaro.c3po")))
        instance.pack()
        assertThat(ZipFile(File(TEMP_FOLDER, "$scriptName.zip"))).all {
            transform { it.entries()?.toList()?.map { it.name } }.isNotNull()
                .containsExactlyInAnyOrder("$scriptName.sc3po", SAMPLE_FILE_NAME)
        }
    }

    private fun removeFileIfExist(fileName: String) {
        val file = File(TEMP_FOLDER, fileName)
        if (file.exists()) file.delete()
    }
}
