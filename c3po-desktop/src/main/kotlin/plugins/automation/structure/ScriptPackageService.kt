package plugins.automation.structure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles packaging automation scripts into distributable archives (.c3po-script)
 * and restoring them from those archives.
 */
class ScriptPackageService(
    private val scriptStorage: ScriptStorage,
) {

    companion object {
        const val PACKAGE_EXTENSION = "c3po-script"
    }

    data class PreparedImport(
        val packageFile: File,
        val tempDir: File,
        val rootDir: File,
        val script: Script,
    )

    sealed interface ImportResolution {
        object Overwrite : ImportResolution
        data class Rename(val newName: String) : ImportResolution
    }

    data class ImportResult(
        val script: Script,
        val targetFolder: File,
    )

    suspend fun exportScript(
        scriptFolder: File,
        destinationDirectory: File,
        scriptName: String,
        overwrite: Boolean,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(scriptFolder.exists() && scriptFolder.isDirectory) {
                "Script folder not found: ${scriptFolder.absolutePath}"
            }

            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdirs()
            }

            val targetFile = resolvePackageFile(scriptName, destinationDirectory)
            if (targetFile.exists()) {
                if (!overwrite) {
                    throw IllegalStateException("File already exists at ${targetFile.absolutePath}")
                }
                if (!targetFile.delete()) {
                    throw IOException("Failed to overwrite existing file: ${targetFile.absolutePath}")
                }
            }

            zipFolder(scriptFolder, targetFile)
            targetFile
        }
    }

    fun resolvePackageFile(scriptName: String, destinationDirectory: File): File =
        File(destinationDirectory, "${scriptName.escapeForFileName()}.$PACKAGE_EXTENSION")

    suspend fun prepareImport(packageFile: File): Result<PreparedImport> = withContext(Dispatchers.IO) {
        var tempDir: File? = null
        runCatching {
            require(packageFile.exists() && packageFile.isFile) {
                "Package file not found: ${packageFile.absolutePath}"
            }

            tempDir = Files.createTempDirectory("c3po-script-import").toFile()

            val rootDirName = extractZip(packageFile, tempDir!!)
            val rootDir = File(tempDir, rootDirName)
            if (!rootDir.exists() || !rootDir.isDirectory) {
                throw IllegalArgumentException("Invalid script package: root directory not found")
            }

            val script = scriptStorage.loadScriptFromFolder(rootDir.absolutePath)
            validateFormat(script)

            PreparedImport(
                packageFile = packageFile,
                tempDir = tempDir!!,
                rootDir = rootDir,
                script = script,
            )
        }.onFailure {
            tempDir?.deleteRecursively()
        }
    }

    suspend fun finalizeImport(
        preparedImport: PreparedImport,
        resolution: ImportResolution,
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val finalName = when (resolution) {
                ImportResolution.Overwrite -> preparedImport.script.name
                is ImportResolution.Rename -> resolution.newName.trim()
            }

            require(finalName.isNotBlank()) { "Script name cannot be blank" }

            val targetFolder = scriptStorage.getScriptFolder(finalName)
            if (targetFolder.exists()) {
                when (resolution) {
                    ImportResolution.Overwrite -> {
                        if (!targetFolder.deleteRecursively()) {
                            throw IOException("Failed to replace existing script folder: ${targetFolder.absolutePath}")
                        }
                    }

                    is ImportResolution.Rename -> {
                        throw IllegalStateException("Target script already exists: ${targetFolder.absolutePath}")
                    }
                }
            } else {
                targetFolder.parentFile?.mkdirs()
            }

            // Copy contents into destination folder
            val copySuccess = preparedImport.rootDir.copyRecursively(targetFolder, overwrite = true)
            if (!copySuccess) {
                throw IOException("Failed to copy script contents to ${targetFolder.absolutePath}")
            }

            val finalScript = if (finalName != preparedImport.script.name) {
                preparedImport.script.copy(name = finalName)
            } else {
                preparedImport.script
            }

            // Ensure script metadata is persisted with the final name
            scriptStorage.saveScript(finalScript)

            preparedImport.tempDir.deleteRecursively()

            ImportResult(
                script = finalScript,
                targetFolder = targetFolder,
            )
        }.onFailure {
            preparedImport.tempDir.deleteRecursively()
        }
    }

    fun discardPreparedImport(preparedImport: PreparedImport?) {
        preparedImport?.tempDir?.deleteRecursively()
    }

    private fun zipFolder(sourceFolder: File, zipFile: File) {
        ZipOutputStream(zipFile.outputStream().buffered()).use { zipOut ->
            val rootName = sourceFolder.name
            zipOut.putNextEntry(ZipEntry("$rootName/"))
            zipOut.closeEntry()

            sourceFolder.walkTopDown().forEach { file ->
                if (file == sourceFolder) return@forEach
                val relativePath =
                    sourceFolder.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')
                val baseEntry = if (relativePath.isEmpty()) rootName else "$rootName/$relativePath"
                val entryName = if (file.isDirectory) "$baseEntry/" else baseEntry
                val entry = ZipEntry(entryName)
                zipOut.putNextEntry(entry)
                if (!file.isDirectory) {
                    file.inputStream().use { input -> input.copyTo(zipOut) }
                }
                zipOut.closeEntry()
            }
        }
    }

    private fun extractZip(packageFile: File, targetDir: File): String {
        var rootDirName: String? = null
        val basePath = targetDir.toPath()

        ZipInputStream(packageFile.inputStream().buffered()).use { zipInput ->
            var entry = zipInput.nextEntry
            if (entry == null) {
                throw IllegalArgumentException("Empty script package")
            }

            while (entry != null) {
                val normalized = entry.name.replace('\\', '/').trimStart('/')
                if (normalized.isEmpty()) {
                    entry = zipInput.nextEntry
                    continue
                }

                val targetPath = basePath.resolve(normalized).normalize()
                if (!targetPath.startsWith(basePath)) {
                    throw SecurityException("Zip entry is outside target directory: ${entry.name}")
                }

                val targetFile = targetPath.toFile()
                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    targetFile.parentFile?.mkdirs()
                    targetFile.outputStream().use { output ->
                        zipInput.copyTo(output)
                    }
                }

                if (rootDirName == null) {
                    rootDirName = normalized.substringBefore('/')
                }

                entry = zipInput.nextEntry
            }
        }

        return rootDirName ?: throw IllegalArgumentException("Invalid script package: missing script contents")
    }

    private fun validateFormat(script: Script) {
        val supportedFormats = setOf("1.0")
        if (script.formatVersion !in supportedFormats) {
            throw IllegalArgumentException("Unsupported script format version: ${script.formatVersion}")
        }
    }

    private fun String.escapeForFileName(): String = replace(Regex("[^a-zA-Z0-9._-]"), "_")
}
