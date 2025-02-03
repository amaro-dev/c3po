package commands

class InstallApkCommand(path: String) : AdbCommand<Boolean> {
    override val command: String = "install $path"

    override fun parse(result: String): Boolean {
        return result.trim().isNotEmpty()
    }
}
