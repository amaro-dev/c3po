package core.facade.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class UpdateInstaller(
    private val fileManager: UpdateFileManager,
    private val platformDetector: PlatformDetector = DefaultPlatformDetector(),
    private val pathManager: UpdatePathManager = UpdatePathManager()
) {

    suspend fun installUpdate(
        installFile: File,
        onProgress: suspend (String) -> Unit = {}
    ): InstallResult {
        onProgress("Validating installation file...")
        // Add delay to simulate real validation time
        kotlinx.coroutines.delay(500)
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

        // Use enhanced disk space validation with UpdatePathManager
        onProgress("Checking disk space and permissions...")
        // Add delay to simulate real disk space checking
        kotlinx.coroutines.delay(800)
        val version = extractVersionFromFilename(installFile.name)
        val readiness =
            pathManager.validateInstallationReadiness(version, "C3PO", pathManager.estimateAppSizeFromZip(installFile))

        when (readiness) {
            is UpdatePathManager.InstallationReadiness.Failed ->
                return InstallResult.failure(readiness.reason)

            is UpdatePathManager.InstallationReadiness.InsufficientSpace ->
                return InstallResult.failure(readiness.spaceInfo.formatUserMessage())

            else -> Unit
        }

        onProgress("Starting installation process...")
        // Add delay to simulate preparation time
        kotlinx.coroutines.delay(500)
        return when (platformDetector.getCurrentPlatform()) {
            Platform.MACOS -> installZipOnMacOS(installFile, onProgress)
            Platform.WINDOWS -> InstallResult.failure("Windows installation not yet implemented")
            Platform.LINUX -> InstallResult.failure("Linux installation not yet implemented")
            Platform.UNKNOWN -> InstallResult.failure("Unsupported platform for auto-installation")
        }
    }

    private fun extractVersionFromFilename(filename: String): String {
        // Extract version from filename like "c3po-2.1.0-macos.zip"
        val versionPattern = Regex("""c3po-(.+)-macos\.zip""", RegexOption.IGNORE_CASE)
        return versionPattern.find(filename)?.groupValues?.get(1) ?: "unknown"
    }

    private suspend fun installZipOnMacOS(
        zipFile: File,
        onProgress: suspend (String) -> Unit
    ): InstallResult {
        var backupFile: File? = null
        var stagingDir: File? = null
        return try {
            onProgress("Extracting update archive...")
            kotlinx.coroutines.delay(1000) // Simulate extraction time
            stagingDir = extractZipToStaging(zipFile)

            onProgress("Finding application bundle...")
            kotlinx.coroutines.delay(500) // Simulate search time
            val appBundle = findAppBundle(stagingDir)
            val targetLocation = getApplicationsDirectory()

            onProgress("Installing application to Applications folder...")
            kotlinx.coroutines.delay(2000) // Simulate copy time
            backupFile = copyAppBundle(appBundle, targetLocation)

            onProgress("Removing quarantine attributes...")
            kotlinx.coroutines.delay(500) // Simulate quarantine removal
            removeQuarantine(File(targetLocation, appBundle.name))

            onProgress("Installation completed successfully")

            // Clean up backup file after successful installation
            if (backupFile != null && backupFile.exists()) {
                onProgress("Cleaning up backup files...")
                try {
                    if (backupFile.deleteRecursively()) {
                        kotlinx.coroutines.delay(200) // Brief delay to show cleanup message
                    }
                } catch (e: Exception) {
                    // Log but don't fail the installation for backup cleanup issues
                    println("[UpdateInstaller] Warning: Failed to clean up backup file: ${backupFile.absolutePath}")
                }
            }

            kotlinx.coroutines.delay(500) // Allow user to see completion message
            InstallResult.success(requiresRestart = true)
        } catch (e: SecurityException) {
            InstallResult.failure("Installation permission denied. Please run as administrator or check file permissions.")
        } catch (e: IOException) {
            InstallResult.failure("Installation I/O error: ${e.message}. Please check disk space and file permissions.")
        } catch (e: InterruptedException) {
            InstallResult.failure("Installation was cancelled")
        } catch (e: Exception) {
            InstallResult.failure("Installation error: ${e.message}")
        } finally {
            // Clean up staging directory
            if (stagingDir != null && stagingDir.exists()) {
                try {
                    stagingDir.deleteRecursively()
                } catch (e: Exception) {
                    println("[UpdateInstaller] Warning: Failed to clean up staging directory: ${stagingDir.absolutePath}")
                }
            }
        }
    }

    private suspend fun extractZipToStaging(zipFile: File): File = withContext(Dispatchers.IO) {
        val stagingDir = File(System.getProperty("java.io.tmpdir"), "c3po-update-staging-${System.currentTimeMillis()}")
        stagingDir.mkdirs()

        // Use Java's built-in ZIP extraction
        java.util.zip.ZipInputStream(zipFile.inputStream().buffered()).use { zipInput ->
            var entry = zipInput.nextEntry
            while (entry != null) {
                val entryFile = File(stagingDir, entry.name)

                // Security check: prevent zip slip vulnerability
                if (!entryFile.canonicalPath.startsWith(stagingDir.canonicalPath + File.separator) &&
                    !entryFile.canonicalPath.equals(stagingDir.canonicalPath)
                ) {
                    throw SecurityException("Zip entry is outside target directory: ${entry.name}")
                }

                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    entryFile.outputStream().use { output ->
                        zipInput.copyTo(output)
                    }

                    // Preserve executable permissions for macOS app bundles
                    if (entry.name.contains("MacOS/") || entry.name.endsWith(".sh")) {
                        entryFile.setExecutable(true, false)
                    }

                    // For all files, ensure proper permissions
                    if (entry.name.contains(".app/")) {
                        // Files inside app bundle should be readable
                        entryFile.setReadable(true, false)
                        entryFile.setWritable(true, true) // Owner writable only
                    }
                }
                entry = zipInput.nextEntry
            }
        }

        // After extraction, find the .app bundle and try to fix its permissions comprehensively
        val appBundle = findAppBundle(stagingDir)
        try {
            fixAppBundlePermissions(appBundle)
        } catch (e: Exception) {
            println("[UpdateInstaller] Warning: Failed to fix app bundle permissions: ${e.message}")
        }

        return@withContext stagingDir
    }

    private suspend fun fixAppBundlePermissions(appBundle: File): Unit = withContext(Dispatchers.IO) {
        try {
            // Fix permissions using chmod - more reliable than Java's setXXX methods
            val chmodCommand = arrayOf(
                "/bin/chmod",
                "-R",
                "755",
                appBundle.absolutePath
            )
            val process = ProcessBuilder(*chmodCommand)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                println("[UpdateInstaller] Fixed permissions for app bundle: ${appBundle.absolutePath}")

                // Make sure the main executable is definitely executable
                val executablePath = File(appBundle, "Contents/MacOS")
                if (executablePath.exists()) {
                    executablePath.listFiles()?.forEach { executable ->
                        if (executable.isFile) {
                            val makeExecCommand = arrayOf(
                                "/bin/chmod",
                                "+x",
                                executable.absolutePath
                            )
                            ProcessBuilder(*makeExecCommand).start().waitFor()
                        }
                    }
                }
            } else {
                println("[UpdateInstaller] Warning: chmod failed with exit code $exitCode")
            }
        } catch (e: Exception) {
            println("[UpdateInstaller] Warning: Exception while fixing permissions: ${e.message}")
        }
    }

    private suspend fun removeQuarantine(appBundle: File): Unit = withContext(Dispatchers.IO) {
        try {
            // First, remove quarantine attribute recursively
            val removeQuarantineCommand = arrayOf(
                "/usr/bin/xattr",
                "-dr",
                "com.apple.quarantine",
                appBundle.absolutePath
            )
            val process = ProcessBuilder(*removeQuarantineCommand)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()

            if (exitCode != 0) {
                println("[UpdateInstaller] Warning: Failed to remove quarantine ($exitCode): $output")
            } else {
                println("[UpdateInstaller] Successfully removed quarantine from: ${appBundle.absolutePath}")
            }

            // Also try to clear any extended attributes that might cause issues
            try {
                val clearXattrCommand = arrayOf(
                    "/usr/bin/xattr",
                    "-cr",
                    appBundle.absolutePath
                )
                val clearProcess = ProcessBuilder(*clearXattrCommand)
                    .redirectErrorStream(true)
                    .start()

                clearProcess.waitFor()
                println("[UpdateInstaller] Cleared all extended attributes from: ${appBundle.absolutePath}")
            } catch (e: Exception) {
                println("[UpdateInstaller] Warning: Failed to clear extended attributes: ${e.message}")
            }

            // Try to explicitly allow the app through Gatekeeper
            try {
                val allowCommand = arrayOf(
                    "/usr/bin/spctl",
                    "--add",
                    "--label", "C3PO Auto-Update",
                    appBundle.absolutePath
                )
                val allowProcess = ProcessBuilder(*allowCommand)
                    .redirectErrorStream(true)
                    .start()

                allowProcess.waitFor()
                println("[UpdateInstaller] Added Gatekeeper exception for: ${appBundle.absolutePath}")
        } catch (e: Exception) {
                println("[UpdateInstaller] Warning: Failed to add Gatekeeper exception: ${e.message}")
            }

        } catch (e: Exception) {
            println("[UpdateInstaller] Warning: Exception during quarantine/Gatekeeper handling: ${e.message}")
            // Don't throw - this is not critical for functionality
        }
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

    private suspend fun copyAppBundle(sourceApp: File, targetDir: File): File? = withContext(Dispatchers.IO) {
        val targetApp = File(targetDir, sourceApp.name)
        var backupApp: File? = null

        // Handle existing app by creating backup
        if (targetApp.exists()) {
            val backupName = "${sourceApp.nameWithoutExtension}-backup-${System.currentTimeMillis()}.app"
            backupApp = File(targetDir, backupName)

            // Use File.renameTo instead of mv command
            if (!targetApp.renameTo(backupApp)) {
                throw IOException("Failed to backup existing application from ${targetApp.absolutePath} to ${backupApp.absolutePath}")
            }
        }

        // Use Kotlin's built-in copyRecursively instead of cp command
        try {
            if (!sourceApp.copyRecursively(targetApp, overwrite = true)) {
                throw IOException("Failed to copy app bundle - copyRecursively returned false")
            }
        } catch (e: Exception) {
            // If copy failed and we made a backup, try to restore it
            if (backupApp != null && backupApp.exists()) {
                backupApp.renameTo(targetApp) // Attempt to restore backup
            }
            throw IOException("Failed to copy app bundle from ${sourceApp.absolutePath} to ${targetApp.absolutePath}: ${e.message}", e)
        }

        // Verify the copy was successful
        if (!targetApp.exists()) {
            // If copy failed and we made a backup, try to restore it
            if (backupApp != null && backupApp.exists()) {
                backupApp.renameTo(targetApp) // Attempt to restore backup
            }
            throw IOException("App bundle was not copied successfully to: ${targetApp.absolutePath}")
        }
        
        // Verify it's a valid app bundle (contains at least the expected structure)
        if (!targetApp.isDirectory) {
            // If verification failed and we made a backup, try to restore it
            if (backupApp != null && backupApp.exists()) {
                targetApp.deleteRecursively() // Remove the invalid copy
                backupApp.renameTo(targetApp) // Attempt to restore backup
            }
            throw IOException("Copied app bundle is not a directory: ${targetApp.absolutePath}")
        }

        // Return the backup file so it can be cleaned up later
        return@withContext backupApp
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