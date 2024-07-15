object Settings {
    const val FILE_NAME = "c3po.cfg"
    const val RESOURCES_PROP = "compose.application.resources.dir"
    const val ADB_PATH_PROP = "command.adb.path"
    const val NAME_SYSTEM_PROP = "ro.product.name"
    private const val PACKAGE_PROP = "jpackage.app-path"
    private const val USER_HOME = "user.home"

    fun isDebug() = (System.getProperty(PACKAGE_PROP) == null)

    fun productionSettingsFolder() = System.getProperty(USER_HOME, "~") + "/.config/c3po"

}
