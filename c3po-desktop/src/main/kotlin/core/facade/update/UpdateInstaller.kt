package core.facade.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class UpdateInstaller(
    private val fileManager: UpdateFileManager,
    private val platformDetector: PlatformDetector = DefaultPlatformDetector()
) {

    suspend fun installUpdate(installFile: File): InstallResult {
        val validation = fileManager.validateFile(installFile)

        when (validation) {
            is UpdateFileManager.ValidationResult.NotFound ->
                return InstallResult.failure("Installation file not found: ${installFile.absolutePath}")

            is UpdateFileManager.ValidationResult.Empty ->
                return InstallResult.failure("Installation file is empty or corrupted")

            is UpdateFileManager.ValidationResult.NotReadable ->
                return InstallResult.failure("Installation file is not readable")

            is UpdateFileManager.ValidationResult.Valid -> {
                // Continue with installation
            }
        }

        val diskSpaceResult = fileManager.checkDiskSpace(installFile, multiplier = 3)
        if (diskSpaceResult is UpdateFileManager.DiskSpaceResult.Insufficient) {
            return InstallResult.failure(diskSpaceResult.formatMessage())
        }

        return when (platformDetector.getCurrentPlatform()) {
            Platform.MACOS -> installDmgOnMacOS(installFile)
            Platform.WINDOWS -> InstallResult.failure("Windows installation not yet implemented")
            Platform.LINUX -> InstallResult.failure("Linux installation not yet implemented")
            Platform.UNKNOWN -> InstallResult.failure("Unsupported platform for auto-installation")
        }
    }

    private suspend fun installDmgOnMacOS(dmgFile: File): InstallResult {
        return try {
            val mountPoint = mountDmg(dmgFile)

            try {
                val appBundle = findAppBundle(mountPoint)
                val targetLocation = getApplicationsDirectory()

                copyAppBundle(appBundle, targetLocation)

                InstallResult.success(requiresRestart = true)
            } finally {
                unmountDmg(mountPoint)
            }
        } catch (e: SecurityException) {
            InstallResult.failure("Installation permission denied. Please run as administrator or check file permissions.")
        } catch (e: IOException) {
            InstallResult.failure("Installation I/O error: ${e.message}. Please check disk space and file permissions.")
        } catch (e: InterruptedException) {
            InstallResult.failure("Installation was cancelled")
        } catch (e: Exception) {
            InstallResult.failure("Installation error: ${e.message}")
        }
    }

    private suspend fun mountDmg(dmgFile: File): File = withContext(Dispatchers.IO) {
        val mountCommand = arrayOf("hdiutil", "attach", dmgFile.absolutePath, "-nobrowse", "-quiet")
        val process = ProcessBuilder(*mountCommand)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        val output = process.inputStream.bufferedReader().readText()

        if (exitCode != 0) {
            throw IOException("Failed to mount DMG: $output")
        }

        val mountPoint = output.lines()
            .lastOrNull { it.contains("/Volumes/") }
            ?.split(Regex("\\s+"))
            ?.lastOrNull()
            ?.let { File(it) }
            ?: throw IOException("Could not determine mount point from hdiutil output")

        if (!mountPoint.exists()) {
            throw IOException("Mount point does not exist: ${mountPoint.absolutePath}")
        }

        mountPoint
    }

    private fun findAppBundle(mountPoint: File): File {
        val appFiles = mountPoint.listFiles { file ->
            file.isDirectory && file.name.endsWith(".app")
        }

        if (appFiles.isNullOrEmpty()) {
            throw IOException("No .app bundle found in mounted DMG: ${mountPoint.absolutePath}")
        }

        if (appFiles.size > 1) {
            val c3poApp = appFiles.find { it.name.contains("c3po", ignoreCase = true) }
            if (c3poApp != null) {
                return c3poApp
            }
        }

        return appFiles[0]
    }

    private fun getApplicationsDirectory(): File {
        return File("/Applications")
    }

    private suspend fun copyAppBundle(sourceApp: File, targetDir: File): Unit = withContext(Dispatchers.IO) {
        val targetApp = File(targetDir, sourceApp.name)

        if (targetApp.exists()) {
            val backupName = "${sourceApp.nameWithoutExtension}-backup-${System.currentTimeMillis()}.app"
            val backupApp = File(targetDir, backupName)

            val moveCommand = arrayOf("mv", targetApp.absolutePath, backupApp.absolutePath)
            val moveProcess = ProcessBuilder(*moveCommand).start()

            if (moveProcess.waitFor() != 0) {
                throw IOException("Failed to backup existing application")
            }
        }

        val copyCommand = arrayOf("cp", "-R", sourceApp.absolutePath, targetApp.absolutePath)
        val copyProcess = ProcessBuilder(*copyCommand)
            .redirectErrorStream(true)
            .start()

        val exitCode = copyProcess.waitFor()
        val output = copyProcess.inputStream.bufferedReader().readText()

        if (exitCode != 0) {
            throw IOException("Failed to copy app bundle: $output")
        }

        if (!targetApp.exists()) {
            throw IOException("App bundle was not copied successfully to: ${targetApp.absolutePath}")
        }
    }

    private suspend fun unmountDmg(mountPoint: File): Unit = withContext(Dispatchers.IO) {
        try {
            val unmountCommand = arrayOf("hdiutil", "detach", mountPoint.absolutePath, "-quiet")
            val process = ProcessBuilder(*unmountCommand)
                .redirectErrorStream(true)
                .start()

            process.waitFor()
        } catch (e: Exception) {
            // Log but don't throw - this is cleanup
        }
    }

    enum class Platform {
        MACOS, WINDOWS, LINUX, UNKNOWN
    }

    data class InstallResult(
        val success: Boolean,
        val message: String? = null,
        val requiresRestart: Boolean = false
    ) {
        companion object {
            fun success(requiresRestart: Boolean = false) = InstallResult(
                success = true,
                requiresRestart = requiresRestart
            )

            fun failure(message: String) = InstallResult(
                success = false,
                message = message
            )
        }
    }
}

interface PlatformDetector {
    fun getCurrentPlatform(): UpdateInstaller.Platform
}

class DefaultPlatformDetector : PlatformDetector {
    override fun getCurrentPlatform(): UpdateInstaller.Platform {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("mac") || osName.contains("darwin") -> UpdateInstaller.Platform.MACOS
            osName.contains("win") -> UpdateInstaller.Platform.WINDOWS
            osName.contains("linux") -> UpdateInstaller.Platform.LINUX
            else -> UpdateInstaller.Platform.UNKNOWN
        }
    }
}