package core.command

import core.model.DiskPartition

class GetStorageInfoCommand : AdbCommand<List<DiskPartition>> {
    override val command: String = "shell df"

    override fun parse(result: String): List<DiskPartition> {
        return result.lines()
            .drop(1) // Skip header
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 6) {
                    val partition = parts[0]
                    val total = parts[1]
                    val used = parts[2]
                    val percentage = parts[4].removeSuffix("%").toIntOrNull() ?: 0
                    val mountPoint = parts[5]

                    DiskPartition(partition, used, total, percentage, mountPoint)
                } else null
            }
            .filter { it.mountPoint.startsWith("/") } // Only real partitions
    }
}