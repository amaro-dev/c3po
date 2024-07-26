package commands


import models.AdbDevice
import models.AppPackage

class ListPackagesCommand(device: AdbDevice): AdbCommand<List<AppPackage>> {
    override val command: String = "-s ${device.id} shell dumpsys package"

    override fun parse(result: CommandResult): List<AppPackage> {
        val packageLinePart = "\\s{2}Package\\s\\[(.*)\\].*"
        val ignoredLinesPart = "(\\r\\n\\s{4}.*)*"
        val versionCodeAndTargetPart = "\\r\\n\\s{4}versionCode=(\\d+)\\stargetSdk=(\\d+)"
        val versionNamePart = "\\r\\n\\s{4}versionName=(.*)"
        val regex = Regex("$packageLinePart$ignoredLinesPart$versionCodeAndTargetPart$versionNamePart")
        return result.content.substring(result.content.indexOf("Packages:"))
            .let {
                regex.findAll(it).map {
                    AppPackage(
                        it.groups[1]?.value ?: "",
                        try { it.groups[5]?.value ?: "" } catch (ex: IndexOutOfBoundsException) { "" },
                        try { it.groups[3]?.value?.toInt() ?: -1 } catch (ex: IndexOutOfBoundsException) { -1 },
                        try { it.groups[4]?.value?.toInt() ?: -1 } catch (ex: IndexOutOfBoundsException) { -1 }
                    )
                }
            }
            .toList()
            .sortedBy { it.packageName }
    }


}
//
//fun main( ) {
//    ListPackagesCommand2(AdbDevice("1494352502"))
//        .run("/Users/roarodrigues/Library/Android/sdk/platform-tools/adb")
//        .forEach { println(it) }
//}
