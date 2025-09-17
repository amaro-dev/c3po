package plugins.automation.structure

import core.command.InstallApkCommand
import java.io.File

/**
 * Centralized utility for APK path resolution and validation in automation scripts.
 * Handles conversion between relative and absolute paths, validation, and command creation.
 */
object ApkPathResolver {

    /**
     * Resolves a relative APK path to an absolute path based on the script folder.
     * @param scriptFolder The script folder containing the APK
     * @param relativePath The relative path from the script (e.g., "./app.apk" or "app.apk")
     * @return Absolute path to the APK file
     * @throws IllegalArgumentException if the path cannot be resolved
     */
    fun resolveRelativeToAbsolute(scriptFolder: String, relativePath: String): String {
        val cleanRelativePath = relativePath.removePrefix("./")
        val absolutePath = File(scriptFolder, cleanRelativePath).absolutePath
        return absolutePath
    }

    /**
     * Validates that an APK file exists at the given absolute path.
     * @param absolutePath The absolute path to validate
     * @return true if the file exists and is readable
     */
    fun validateApkExists(absolutePath: String): Boolean {
        val file = File(absolutePath)
        return file.exists() && file.canRead()
    }

    /**
     * Generates a relative path for storing in script configuration.
     * Always uses "./" prefix for consistency.
     * @param scriptFolder The script folder base directory
     * @param absolutePath The absolute path to make relative
     * @return Relative path starting with "./"
     */
    fun generateRelativePath(scriptFolder: String, absolutePath: String): String {
        val file = File(absolutePath)
        return "./${file.name}"
    }

    /**
     * Creates a properly formatted InstallApkCommand with the given absolute path.
     * Separates command options from the file path to prevent path parsing issues.
     * @param absolutePath The absolute path to the APK file
     * @return InstallApkCommand with proper formatting
     */
    fun createInstallCommand(absolutePath: String): InstallApkCommand {
        // Use -r flag for replace existing package, with quoted path to handle spaces
        return InstallApkCommand("-r \"$absolutePath\"")
    }

    /**
     * Validates and resolves an APK path for script execution.
     * Combines resolution and validation in a single step.
     * @param scriptFolder The script folder containing the APK
     * @param relativePath The relative path from the script
     * @return Absolute path if valid
     * @throws IllegalArgumentException if path is invalid or file doesn't exist
     */
    fun validateAndResolve(scriptFolder: String, relativePath: String): String {
        val absolutePath = resolveRelativeToAbsolute(scriptFolder, relativePath)

        if (!validateApkExists(absolutePath)) {
            throw IllegalArgumentException("APK file not found: $relativePath (resolved to: $absolutePath)")
        }

        return absolutePath
    }

    /**
     * Copies an APK file to the script folder and returns the relative path.
     * @param sourceApkPath The source APK absolute path
     * @param scriptFolder The target script folder
     * @return Relative path for storage in script configuration
     * @throws IllegalArgumentException if copy operation fails
     */
    fun copyApkToScriptFolder(sourceApkPath: String, scriptFolder: String): String {
        val sourceFile = File(sourceApkPath)
        if (!sourceFile.exists()) {
            throw IllegalArgumentException("Source APK file does not exist: $sourceApkPath")
        }

        val scriptDir = File(scriptFolder)
        if (!scriptDir.exists()) {
            scriptDir.mkdirs()
        }

        val fileName = sourceFile.name
        val targetFile = File(scriptDir, fileName)

        // Copy file
        sourceFile.copyTo(targetFile, overwrite = true)

        // Return relative path
        return generateRelativePath(scriptFolder, targetFile.absolutePath)
    }
}