package facade

import java.util.Properties

interface SettingsRepository {
    fun load(): Result<Properties>
    fun save(settings: Properties): Result<Unit>
}
