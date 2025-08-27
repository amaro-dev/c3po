object AppVersion {
    fun getAppVersion(): String {
        return try {
            // Try to get version from JAR manifest
            val clazz = AppVersion::class.java
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