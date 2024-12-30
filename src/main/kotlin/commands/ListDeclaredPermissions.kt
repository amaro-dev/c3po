package commands

import models.DeclaredPermissions
import java.io.File

class ListDeclaredPermissions : AdbCommand<List<DeclaredPermissions>> {
    override val command: String = "shell dumpsys package"

    override fun parse(result: CommandResult): List<DeclaredPermissions> =
        result.content.run {
            val exprPermission = " \\[([^\\]]*)\\] "
            val exprOwner = "sourcePackage\\=([^\\s]*)"
            val exprProtection = "prot\\=([\\w\\|]+)"
            val lineSplitRule = Regex("\\r?\\n")
            val content =
                substring(indexOf("Permissions:") + 12)
                    .substringBefore("\r\n\r\n")
                    .substringBefore("\n\n")
                    .replace("\r\n    ", " ")
                    .replace("\n    ", " ")
            lineSplitRule
                .split(content)
                .mapNotNull {
                    val foundPermission = Regex(exprPermission).find(it)?.groupValues?.last()
                    val foundOwner = Regex(exprOwner).find(it)?.groupValues?.last()
                    val foundProtections =
                        Regex(exprProtection)
                            .find(it)
                            ?.groupValues
                            ?.last()
                            ?.split('|')
                    if (foundOwner != null && foundPermission != null) {
                        Triple(
                            foundOwner,
                            foundPermission,
                            foundProtections,
                        )
                    } else {
                        null
                    }
                }.groupBy({ it.first }, { Pair(it.second, it.third) })
                .entries
                .map {
                    DeclaredPermissions(
                        it.key,
                        it.value
                            .sortedBy { it.first }
                            .map {
                                it.first to (
                                        it.second?.map { PermissionFlag.protectedValueOf(it.uppercase()) }
                                            ?: emptyList()
                                        )
                            }.toMap(),
                    )
                }
        }
}

fun main() {
    with(File("/Users/roarodrigues/dump-dumpsys").readText()) {
        val exprPermission = " \\[([^\\]]*)\\] "
        val exprOwner = "sourcePackage\\=([^\\s]*)"
        val exprProtection = "prot\\=([\\w\\|]+)"
        val lineSplitRule = Regex("\\r?\\n")
        val content =
            substring(indexOf("Permissions:") + 12)
                .substringBefore("\r\n\r\n")
                .substringBefore("\n\n")
                .replace("\r\n    ", " ")
        println(
            lineSplitRule
                .split(content)
                .mapNotNull {
                    val foundPermission = Regex(exprPermission).find(it)?.groupValues?.last()
                    val foundOwner = Regex(exprOwner).find(it)?.groupValues?.last()
                    val foundProtections =
                        Regex(exprProtection)
                            .find(it)
                            ?.groupValues
                            ?.last()
                            ?.split('|')
                    if (foundOwner != null && foundPermission != null) {
                        Triple(
                            foundOwner,
                            foundPermission,
                            foundProtections,
                        )
                    } else {
                        null
                    }
                }.groupBy({ it.first }, { Pair(it.second, it.third) }),
        )
    }
}

enum class PermissionFlag(
    val code: String,
    val color: Long? = null,
) {
    APPOP("OP", 0xFF023E8A),
    DANGEROUS("DG", 0xFFCC0000),
    DEVELOPMENT("DV"),
    INSTALLER("IN"),
    NORMAL("NM", 0xFF009D00),
    PRE23("23"),
    PREINSTALLED("PI"),
    PRIVILEGED("PV", 0xFF000000),
    SIGNATURE("SG", 0xFFFF8D00),
    VERIFIER("VF"),
    UNKNOWN("UN"),
    ;

    companion object {
        fun protectedValueOf(v: String): PermissionFlag =
            try {
                PermissionFlag.valueOf(v)
            } catch (e: Exception) {
                UNKNOWN
            }
    }
}
