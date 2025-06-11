package commands

import models.DeclaredPermissions
import java.io.File

class ListDeclaredPermissions : AdbCommand<List<DeclaredPermissions>> {
    override val command: String = "shell dumpsys package"

    override fun parse(result: String): List<DeclaredPermissions> =
        result.run {
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
                            .associate {
                                it.first to (
                                        it.second?.map { PermissionFlag.protectedValueOf(it.uppercase()) }
                                            ?: emptyList()
                                        )
                            },
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
    val isBase: Boolean = false
) {
    APPOP(),
    CONFIGURATOR(),
    COMPANION(),
    DANGEROUS(true), // base
    DEVELOPMENT(),
    INCIDENTREPORTAPPROVER(),
    INSTALLER(),
    INSTANT(),
    INTERNAL(true), // Base
    KNOWNSIGNER(),
    MODULE(),
    NORMAL(true), // Base
    OEM(),
    PRE23(),
    PREINSTALLED(),
    PRIVILEGED(),
    RECENTS(),
    RETAILDEMO(),
    ROLE(),
    RUNTIME(),
    SETUP(),
    SIGNATURE(true), // Base
    VERIFIER(),
    VENDORPRIVILEGED(),
    UNKNOWN(),
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
