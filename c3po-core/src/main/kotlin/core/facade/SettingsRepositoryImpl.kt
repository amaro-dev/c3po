package core.facade

import java.io.File
import java.io.FileNotFoundException
import java.util.Properties

class SettingsRepositoryImpl(
    private val resourcesPath: File,
    private val settingsFile: String,
) : SettingsRepository {
    override fun load(): Result<Properties> {
        val props = Properties()
        val propsFile = File(resourcesPath, settingsFile)
        return if (propsFile.exists()) {
            props.load(File(resourcesPath, settingsFile).inputStream())
            Result.success(props)
        } else {
            Result.failure(FileNotFoundException())
        }
    }

    override fun save(settings: Properties): Result<Unit> =
        try {
            val settingsFile = File(resourcesPath, settingsFile)
            if (!settingsFile.exists()) settingsFile.createNewFile()
            settings.store(settingsFile.outputStream(), null)
            Result.success(Unit)
        } catch (ex: Throwable) {
            Result.failure(ex)
        }

    override fun createPathIfNeeded(): Result<Unit> =
        try {
            if (!resourcesPath.exists()) resourcesPath.mkdirs()
            Result.success(Unit)
        } catch (ex: Exception) {
            Result.failure(ex)
        }
}
