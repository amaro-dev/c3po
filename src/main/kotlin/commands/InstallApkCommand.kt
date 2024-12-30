package commands

class InstallApkCommand(path: String) : AdbCommand<Boolean> {
    override val command: String = "install $path"

    override fun parse(result: CommandResult): Boolean {
        println(result.content.trim())
        return result.content.trim().isNotEmpty()
    }
}
