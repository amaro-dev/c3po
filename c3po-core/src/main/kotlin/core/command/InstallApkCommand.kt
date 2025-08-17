package core.command

class InstallApkCommand(
    path: String,
) : AdbCommand<Boolean> {
    override val command: String = "install $path"

    override fun parse(result: String): Boolean = result.trim().isNotEmpty()
}
