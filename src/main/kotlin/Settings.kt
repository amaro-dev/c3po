object Settings {
    const val FILE_NAME = "c3po.cfg"
    const val RESOURCES_PROP = "compose.application.resources.dir"
    const val ADB_PATH_PROP = "command.adb.path"
    const val PACKAGE_PROP = "jpackage.app-path"

    fun isDebug() = (System.getProperty(PACKAGE_PROP) == null)

}
