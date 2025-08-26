object Settings {
    const val FILE_NAME = "c3po.cfg"
    const val ADB_PATH_PROP = "command.adb.path"
    private const val PACKAGE_PROP = "jpackage.app-path"
    private const val USER_HOME = "user.home"

    fun isDebug() = (System.getProperty(PACKAGE_PROP) == null)

    fun productionSettingsFolder() = System.getProperty(USER_HOME, "~") + "/.config/c3po"

    fun getAppVersion(): String {
        return try {
            // Try to get version from JAR manifest
            val clazz = Settings::class.java
            val packageInfo = clazz.`package`
            packageInfo?.implementationVersion
                ?: clazz.getResourceAsStream("/META-INF/MANIFEST.MF")?.use { stream ->
                    val manifest = java.util.jar.Manifest(stream)
                    manifest.mainAttributes.getValue("Implementation-Version")
                }
                ?: "dev" // Fallback to build.gradle.kts version
        } catch (e: Exception) {
            "dev" // Fallback to build.gradle.kts version
        }
    }
}
