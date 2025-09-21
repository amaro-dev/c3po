package core.facade

import core.command.TakeScreenshotCommand
import core.util.AppPaths
import java.io.File
import java.io.IOException

/**
 * Manages screenshot file operations, including determining the best save location
 * and opening screenshots in the default macOS image viewer.
 *
 * Leverages existing PermissionChecker patterns for folder access validation.
 */
object ScreenshotFileManager {

    /**
     * Determines the best directory to save screenshots based on available permissions.
     *
     * Priority order:
     * 1. Pictures folder (if accessible)
     * 2. Desktop folder (if accessible)
     * 3. Downloads folder (if accessible)
     * 4. App temp directory (fallback)
     */
    fun getBestScreenshotDirectory(): File {
        val homeDir = System.getProperty("user.home")

        // Try Pictures folder first (most appropriate for screenshots)
        val picturesDir = File(homeDir, "Pictures")
        if (canWriteToDirectory(picturesDir)) {
            return ensureC3poSubdirectory(picturesDir)
        }

        // Fall back to Desktop if accessible
        if (PermissionChecker.checkDesktopFolderAccess()) {
            val desktopDir = File(homeDir, "Desktop")
            return ensureC3poSubdirectory(desktopDir)
        }

        // Try Downloads folder
        if (PermissionChecker.checkDownloadFolderAccess()) {
            val downloadsDir = File(homeDir, "Downloads")
            return ensureC3poSubdirectory(downloadsDir)
        }

        // Final fallback: unified temp directory
        return AppPaths.resolveTempDirectory()
    }

    /**
     * Creates a C3PO subdirectory within the given parent directory to organize screenshots
     */
    private fun ensureC3poSubdirectory(parentDir: File): File {
        val c3poDir = File(parentDir, "C3PO")
        if (!c3poDir.exists()) {
            c3poDir.mkdirs()
        }
        return c3poDir
    }

    /**
     * Checks if we can write to a specific directory
     */
    private fun canWriteToDirectory(directory: File): Boolean {
        return try {
            directory.exists() && directory.canWrite() && directory.isDirectory
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Checks if the Pictures folder is accessible (extends PermissionChecker pattern)
     */
    fun checkPicturesFolderAccess(): Boolean {
        return try {
            val picturesFolder = File(System.getProperty("user.home") + "/Pictures")
            picturesFolder.canRead() && picturesFolder.canWrite()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates a screenshot command configured to save to the device
     */
    fun createScreenshotCommand(): TakeScreenshotCommand {
        return TakeScreenshotCommand()
    }

    /**
     * Creates the local file path for saving the screenshot
     */
    fun createScreenshotFile(): File {
        val directory = getBestScreenshotDirectory()
        val filename = TakeScreenshotCommand.generateScreenshotFilename()
        return File(directory, filename)
    }

    /**
     * Opens a screenshot file in the default macOS image viewer using the `open` command
     */
    fun openScreenshotInViewer(screenshotPath: String): Result<String> {
        return try {
            val process = ProcessBuilder("open", screenshotPath)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Result.success("Screenshot opened successfully")
            } else {
                val error = process.inputStream.bufferedReader().readText()
                Result.failure(IOException("Failed to open screenshot: $error"))
            }
        } catch (e: Exception) {
            Result.failure(IOException("Failed to open screenshot in viewer: ${e.message}", e))
        }
    }

    /**
     * Gets user-friendly description of where screenshots will be saved
     */
    fun getScreenshotLocationDescription(): String {
        val directory = getBestScreenshotDirectory()
        return when {
            directory.path.contains("Pictures") -> "Screenshots saved to Pictures/C3PO folder"
            directory.path.contains("Desktop") -> "Screenshots saved to Desktop/C3PO folder"
            directory.path.contains("Downloads") -> "Screenshots saved to Downloads/C3PO folder"
            else -> "Screenshots saved to temporary folder"
        }
    }
}
