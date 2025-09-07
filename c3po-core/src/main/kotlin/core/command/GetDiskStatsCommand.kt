package core.command

import core.model.DiskStats

class GetDiskStatsCommand : AdbCommand<DiskStats> {
    override val command: String = "shell dumpsys diskstats"

    override fun parse(result: String): DiskStats {
        // Check if the service is unavailable
        if (isServiceUnavailable(result)) {
            return DiskStats(
                totalBytes = 0L,
                usedBytes = 0L,
                availableBytes = 0L,
                percentageUsed = 0,
                isAvailable = false,
                errorMessage = "Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found)."
            )
        }

        return try {
            parseDiskStatsOutput(result)
        } catch (e: Exception) {
            // If parsing fails, return unavailable state
            DiskStats(
                totalBytes = 0L,
                usedBytes = 0L,
                availableBytes = 0L,
                percentageUsed = 0,
                isAvailable = false,
                errorMessage = "Failed to parse diskstats output"
            )
        }
    }

    private fun isServiceUnavailable(result: String): Boolean {
        val lowerResult = result.lowercase()
        return lowerResult.contains("unknown service: diskstats") ||
                lowerResult.contains("can't find service") ||
                lowerResult.contains("dumpsys: not found") ||
                result.trim().isEmpty()
    }

    private fun parseDiskStatsOutput(output: String): DiskStats {
        val lines = output.lines().map { it.trim() }

        // Try parsers in priority order
        val parsers = listOf(
            RealDiskStatsParser(),
            LegacyBytesFormatParser(),
            SingleLineMultiValueParser(),
            GenericFormatParser()
        )

        for (parser in parsers) {
            if (parser.canParse(lines)) {
                return parser.parse(lines)
            }
        }

        // If no parser can handle the format
        return DiskStats(
            totalBytes = 0L,
            usedBytes = 0L,
            availableBytes = 0L,
            percentageUsed = 0,
            isAvailable = false,
            errorMessage = "No disk usage data found in diskstats output"
        )
    }

    private interface DiskStatsParser {
        fun canParse(lines: List<String>): Boolean
        fun parse(lines: List<String>): DiskStats
    }

    private inner class RealDiskStatsParser : DiskStatsParser {
        override fun canParse(lines: List<String>): Boolean {
            return lines.any { it.matches(Regex(""".*-Free:\s+\d+K\s*/\s*\d+K\s+total.*""", RegexOption.IGNORE_CASE)) }
        }

        override fun parse(lines: List<String>): DiskStats {
            var totalBytes = 0L
            var availableBytes = 0L

            // Look for Data partition first (primary storage)
            for (line in lines) {
                if (line.startsWith("Data-Free:", ignoreCase = true)) {
                    val result = parseKBFormat(line)
                    if (result != null) {
                        val (free, total) = result
                        availableBytes = free * 1024L
                        totalBytes = total * 1024L
                        break
                    }
                }
            }

            val usedBytes = totalBytes - availableBytes
            val percentageUsed = if (totalBytes > 0L) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
            } else 0

            return DiskStats(
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                availableBytes = availableBytes,
                percentageUsed = percentageUsed,
                isAvailable = totalBytes > 0L,
                errorMessage = null
            )
        }
    }

    private inner class LegacyBytesFormatParser : DiskStatsParser {
        override fun canParse(lines: List<String>): Boolean {
            return lines.any { line ->
                line.contains("Data-Total", ignoreCase = true) ||
                        line.contains("Data-Free", ignoreCase = true) ||
                        line.contains("System-Free", ignoreCase = true) ||
                        line.contains("System Free", ignoreCase = true)
            }
        }

        override fun parse(lines: List<String>): DiskStats {
            var totalBytes = 0L
            var availableBytes = 0L

            for (line in lines) {
                when {
                    line.contains("Data-Total", ignoreCase = true) -> {
                        extractBytesValue(line)?.let { totalBytes = maxOf(totalBytes, it) }
                    }

                    line.contains("Data-Free", ignoreCase = true) -> {
                        extractBytesValue(line)?.let { availableBytes += it }
                    }

                    line.contains("System-Free", ignoreCase = true) || line.contains(
                        "System Free",
                        ignoreCase = true
                    ) -> {
                        extractBytesValue(line)?.let { availableBytes += it }
                    }
                }
            }

            val usedBytes = totalBytes - availableBytes
            val percentageUsed = if (totalBytes > 0L) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
            } else 0

            return DiskStats(
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                availableBytes = availableBytes,
                percentageUsed = percentageUsed,
                isAvailable = totalBytes > 0L,
                errorMessage = null
            )
        }
    }

    private class SingleLineMultiValueParser : DiskStatsParser {
        override fun canParse(lines: List<String>): Boolean {
            return lines.any { line ->
                line.contains("total:", ignoreCase = true) &&
                        line.contains("free:", ignoreCase = true) &&
                        line.contains("bytes", ignoreCase = true)
            }
        }

        override fun parse(lines: List<String>): DiskStats {
            var totalBytes = 0L
            var availableBytes = 0L

            val targetLine = lines.first { line ->
                line.contains("total:", ignoreCase = true) &&
                        line.contains("free:", ignoreCase = true) &&
                        line.contains("bytes", ignoreCase = true)
            }

            // Parse values using simple regex patterns
            val totalPattern = Regex("""total:\s*(\d+)\s*bytes""", RegexOption.IGNORE_CASE)
            val freePattern = Regex("""free:\s*(\d+)\s*bytes""", RegexOption.IGNORE_CASE)

            totalPattern.find(targetLine)?.let { match ->
                totalBytes = match.groupValues[1].toLongOrNull() ?: 0L
            }

            freePattern.find(targetLine)?.let { match ->
                availableBytes = match.groupValues[1].toLongOrNull() ?: 0L
            }

            val usedBytes = totalBytes - availableBytes
            val percentageUsed = if (totalBytes > 0L) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
            } else 0

            return DiskStats(
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                availableBytes = availableBytes,
                percentageUsed = percentageUsed,
                isAvailable = totalBytes > 0L,
                errorMessage = null
            )
        }
    }

    private inner class GenericFormatParser : DiskStatsParser {
        override fun canParse(lines: List<String>): Boolean {
            return lines.any { line ->
                line.contains("total", ignoreCase = true) ||
                        line.contains("free", ignoreCase = true) ||
                        line.contains("used", ignoreCase = true)
            }
        }

        override fun parse(lines: List<String>): DiskStats {
            var totalBytes = 0L
            var usedBytes = 0L
            var availableBytes = 0L

            for (line in lines) {
                // First try to extract bytes value directly
                val lineBytes = extractBytesValue(line)

                when {
                    line.contains("Total bytes", ignoreCase = true) -> {
                        lineBytes?.let { totalBytes = maxOf(totalBytes, it) }
                    }

                    line.contains("Used bytes", ignoreCase = true) && !line.contains(
                        "Cache-Used",
                        ignoreCase = true
                    ) -> {
                        lineBytes?.let { usedBytes = it }
                    }

                    line.contains("Free bytes", ignoreCase = true) -> {
                        lineBytes?.let { availableBytes += it }
                    }

                    line.contains("System Free", ignoreCase = true) -> {
                        lineBytes?.let { availableBytes += it }
                    }

                    line.contains("total", ignoreCase = true) -> {
                        // If line has "bytes", try to extract near "bytes" first
                        if (line.contains("bytes", ignoreCase = true)) {
                            extractValueNear(line, "bytes")?.let { totalBytes = maxOf(totalBytes, it) }
                        }
                        // Then try near "total" if we didn't find anything
                        if (totalBytes == 0L) {
                            extractValueNear(line, "total")?.let { totalBytes = maxOf(totalBytes, it) }
                        }
                    }

                    line.contains("free", ignoreCase = true) -> {
                        if (line.contains("bytes", ignoreCase = true)) {
                            extractValueNear(line, "bytes")?.let { availableBytes += it }
                        } else {
                            extractValueNear(line, "free")?.let { availableBytes += it }
                        }
                    }
                }
            }

            // Calculate missing values
            if (usedBytes == 0L && totalBytes > 0L && availableBytes > 0L) {
                usedBytes = totalBytes - availableBytes
            }

            val percentageUsed = if (totalBytes > 0L) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
            } else 0

            return DiskStats(
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                availableBytes = availableBytes,
                percentageUsed = percentageUsed,
                isAvailable = totalBytes > 0L,
                errorMessage = null
            )
        }
    }

    private fun parseKBFormat(line: String): Pair<Long, Long>? {
        // Parse format like: "Data-Free: 5108980K / 6082144K total = 83% free"
        val regex = Regex("""(\d+)K\s*/\s*(\d+)K\s+total""", RegexOption.IGNORE_CASE)
        val match = regex.find(line)
        return match?.let {
            val free = it.groupValues[1].toLongOrNull()
            val total = it.groupValues[2].toLongOrNull()
            if (free != null && total != null) {
                Pair(free, total)
            } else null
        }
    }

    private fun extractBytesValue(line: String): Long? {
        // Extract byte values from various formats:
        // "Data-Free: 12345678 bytes"
        // "System Free: 12345678"  
        // "Total bytes: 12345678"
        // Look for the largest number that appears after a colon or before "bytes"
        val patterns = listOf(
            Regex("""total:\s*(\d+)\s*bytes""", RegexOption.IGNORE_CASE),
            Regex("""free:\s*(\d+)\s*bytes""", RegexOption.IGNORE_CASE),
            Regex(""":\s*(\d+)\s*bytes""", RegexOption.IGNORE_CASE),
            Regex("""(\d+)\s+bytes""", RegexOption.IGNORE_CASE),
            Regex(""":\s*(\d+)"""), // After colon
            Regex("""(\d{9,})""") // Any very large number (9+ digits, likely bytes)
        )

        return patterns.asSequence()
            .flatMap { regex -> regex.findAll(line) }
            .mapNotNull { match -> match.groupValues[1].toLongOrNull() }
            .maxOrNull()
    }

    private fun extractValueNear(line: String, keyword: String): Long? {
        // Find the keyword and extract the number that comes after it
        // Handle formats like "total: 134217728000" or "free: 67108864000 bytes"
        // Also handle complex formats like: "Data partition info: 512 blocks, total: 134217728000 bytes, free: 67108864000 bytes"
        val patterns = listOf(
            Regex("""(?:.*?\b)?$keyword\s*:\s*(\d+)\s*(?:bytes)?""", RegexOption.IGNORE_CASE),
            Regex("""(?:.*,\s*)?$keyword\s*:\s*(\d+)\s*(?:bytes)?""", RegexOption.IGNORE_CASE),
            Regex("""(?:.*\b)?total\s*:\s*(\d+)\s*(?:bytes)?""", RegexOption.IGNORE_CASE)
        )

        var maxValue: Long? = null
        for (pattern in patterns) {
            val matches = pattern.findAll(line)
            for (match in matches) {
                val value = match.groupValues[1].toLongOrNull()
                if (value != null && (maxValue == null || value > maxValue)) {
                    maxValue = value
                }
            }
        }

        return maxValue
    }
}