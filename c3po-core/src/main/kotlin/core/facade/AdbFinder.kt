package core.facade

import core.command.SystemCommandExecutor
import java.io.File

/**
 * macOS-only ADB path finder. Tries multiple heuristics in order of likelihood
 * and returns the first validated adb executable path.
 */
class AdbFinder(
    private val system: SystemCommandExecutor,
) {

    suspend fun find(): Result<String> {
        val candidates = mutableListOf<String>()

        // 1) Environment variables
        candidates += envCandidate("ANDROID_SDK_ROOT")
        candidates += envCandidate("ANDROID_HOME")

        // 2) PATH lookup (command -v and which -a)
        candidates += fromCommand("command -v adb").singleOrNull()?.let { listOf(it) } ?: emptyList()
        candidates += fromCommand("which -a adb")

        // 3) Homebrew installation
        candidates += brewCandidate()

        // 4) Default macOS SDK location
        candidates += defaultMacOsCandidate()

        // 5) Spotlight search
        candidates += spotlightCandidates()

        // Validate candidates and return first valid
        for (raw in candidates) {
            val path = raw.trim()
            if (path.isEmpty()) continue
            if (validateAdb(path)) return Result.success(File(path).canonicalPath)
        }

        return Result.failure(IllegalStateException(notFoundMessage))
    }

    private fun envCandidate(varName: String): List<String> {
        val base = System.getenv(varName) ?: return emptyList()
        val path = File(base, "platform-tools/adb").absolutePath
        return listOf(path)
    }

    private suspend fun fromCommand(cmd: String): List<String> =
        system.executeCommand(cmd).getOrNull()
            ?.lineSequence()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toList()
            ?: emptyList()

    private suspend fun brewCandidate(): List<String> {
        val prefix = system.executeCommand("brew --prefix android-platform-tools").getOrNull()?.trim().orEmpty()
        if (prefix.isBlank()) return emptyList()
        return listOf(File(prefix, "bin/adb").absolutePath)
    }

    private fun defaultMacOsCandidate(): List<String> {
        val home = System.getProperty("user.home") ?: return emptyList()
        val path = File(home, "Library/Android/sdk/platform-tools/adb").absolutePath
        return listOf(path)
    }

    private suspend fun spotlightCandidates(): List<String> {
        val query = "kMDItemFSName == 'adb' && kMDItemContentTypeTree == 'public.unix-executable'"
        val result = system.executeCommand("mdfind \"$query\"").getOrNull().orEmpty()
        // Filter for SDK platform-tools first
        val lines = result.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }
        val prioritized = lines.sortedBy { line ->
            if (line.contains("platform-tools/adb")) 0 else 1
        }
        return prioritized.toList()
    }

    private suspend fun validateAdb(path: String): Boolean {
        val f = File(path)
        if (!f.exists()) return false
        if (!f.canExecute()) return false
        // Ensure the binary is functional
        val version = system.executeCommand("\"$path\" version").getOrNull().orEmpty()
        return version.contains("Android Debug Bridge", ignoreCase = true)
    }

    private val notFoundMessage =
        "ADB was not found automatically. We tried ANDROID_HOME/SDK, PATH, Homebrew, default locations, and Spotlight. " +
                "Install via Homebrew: 'brew install android-platform-tools' or select the executable manually."
}
