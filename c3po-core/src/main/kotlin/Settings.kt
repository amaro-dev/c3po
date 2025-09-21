object Settings {
    const val FILE_NAME = "c3po.cfg"
    const val ADB_PATH_PROP = "command.adb.path"
    const val UPDATES_URL_PROP = "updates.url"
    const val DARK_MODE_PROP = "ui.dark.mode"

    // Analytics configuration properties
    const val ANALYTICS_ENABLED_PROP = "analytics.enabled"
    const val ANALYTICS_SERVER_URL_PROP = "analytics.server_url"
    const val ANALYTICS_APP_KEY_PROP = "analytics.app_key"
    const val ANALYTICS_INSTANCE_ID_PROP = "analytics.instance_id"

    // Logging configuration properties
    const val LOGGING_ENABLED_PROP = "logging.enabled"
    const val LOGGING_ADB_PROP = "logging.adb"  // "off", "errors", "full"
    const val LOGGING_PERFORM_PROP = "logging.perform"  // "true", "false"  
    const val LOGGING_REDUCE_PROP = "logging.reduce"  // "true", "false"
    
    private const val PACKAGE_PROP = "jpackage.app-path"
    private const val USER_HOME = "user.home"

    fun isDebug() = (System.getProperty(PACKAGE_PROP) == null)

    fun productionSettingsFolder() = System.getProperty(USER_HOME, "~") + "/.config/c3po"
}
