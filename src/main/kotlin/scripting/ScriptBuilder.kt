package scripting

import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.name

class ScriptBuilder(
    private val name: String,
    private val path: Path
) {
    private val instructions: MutableList<Instruction> = mutableListOf()

    fun append(instruction: Instruction) {
        instructions.add(instruction)
    }

    fun pack() {
        val scriptFile = createScriptFile()
        createZipPackage(scriptFile)
    }

    private fun createScriptFile(): File {
        val scriptFile = File(path.toString(), "$name.sc3po")
        scriptFile.createNewFile()
        with(scriptFile.writer()) {
            instructions.forEach {
                append(it.token)
                append(" ")
                append(it.args.joinToString(" "))
                appendLine()
            }
            flush()
        }
        return scriptFile
    }

    private fun createZipPackage(scriptFile: File) {
        val zipFile = File(path.toString(), "$name.zip")
        zipFile.createNewFile()
        val content = ZipOutputStream(zipFile.outputStream())
        val scriptEntry = ZipEntry(scriptFile.name)
        content.putNextEntry(scriptEntry)
        content.write(scriptFile.readBytes())
        content.closeEntry()
        instructions.flatMap { it.resources }.forEach {
            val entry = ZipEntry(it.name)
            content.putNextEntry(entry)
            content.write(it.toFile().readBytes())
            content.closeEntry()
        }
        content.close()
    }

}
