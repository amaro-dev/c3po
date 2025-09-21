package core.util

import java.io.File

object AppPaths {
    private val candidateEnvVars = listOf("java.io.tmpdir", "TMPDIR", "TMP", "TEMP")

    fun resolveTempDirectory(): File {
        val candidates = buildList {
            candidateEnvVars.forEach { key ->
                val v = if (key == "java.io.tmpdir") System.getProperty(key) else System.getenv(key)
                if (!v.isNullOrBlank()) add(v)
            }
            add("/tmp")
            add("/var/tmp")
        }

        for (path in candidates) {
            val f = File(path)
            if (f.exists() && f.isDirectory && f.canWrite()) return f
        }

        return File(System.getProperty("user.home"), ".tmp").apply { mkdirs() }
    }

    fun getAnalyticsDirectory(): File {
        return File(resolveTempDirectory(), "c3po-analytics").apply { if (!exists()) mkdirs() }
    }

    fun getUpdateDownloadDirectory(): File {
        return File(resolveTempDirectory(), "c3po-updates").apply { if (!exists()) mkdirs() }
    }

    fun createStagingDirectory(prefix: String): File {
        val dir = File(resolveTempDirectory(), "$prefix-${'$'}{System.currentTimeMillis()}")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
