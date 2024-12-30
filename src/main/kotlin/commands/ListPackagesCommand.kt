package commands

import models.AppPackage

class ListPackagesCommand : AdbCommand<List<AppPackage>> {
    override val command: String = "shell dumpsys package"

    override fun parse(result: CommandResult): List<AppPackage> {
        val packageLinePart = "\\s{2}Package\\s\\[(.*)\\].*"
        val ignoredLinesPart = "(?:\\r?\\n\\s{4}.*)*"
        val versionCodeAndTargetPart = "\\r?\\n\\s{4}versionCode=(\\d+).*\\stargetSdk=(\\d+)"
        val versionNamePart = "\\r?\\n\\s{4}versionName=(.*)"
        val regex = Regex("$packageLinePart$ignoredLinesPart$versionCodeAndTargetPart$ignoredLinesPart$versionNamePart")
        return result.content
            .substring(result.content.indexOf("Packages:"))
            .let {
                regex.findAll(it).map {
                    AppPackage(
                        it.groups[1]?.value ?: "",
                        try {
                            it.groups[4]?.value ?: ""
                        } catch (ex: IndexOutOfBoundsException) {
                            ""
                        },
                        try {
                            it.groups[2]?.value?.toInt() ?: -1
                        } catch (ex: IndexOutOfBoundsException) {
                            -1
                        },
                        try {
                            it.groups[3]?.value?.toInt() ?: -1
                        } catch (ex: IndexOutOfBoundsException) {
                            -1
                        },
                    )
                }
            }.toList()
            .sortedBy { it.packageName }
    }
}
