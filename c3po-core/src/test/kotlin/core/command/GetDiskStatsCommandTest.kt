package core.command

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class GetDiskStatsCommandTest {

    private val command = GetDiskStatsCommand()

    @Test
    fun `should parse diskstats output with data and system partitions`() {
        val diskstatsOutput = """
            Data-Total: 67108864000 bytes
            Data-Free: 43690123776 bytes
            System-Total: 3221225472 bytes
            System-Free: 2147483648 bytes
        """.trimIndent()

        val result = command.parse(diskstatsOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isTrue()
            transform { it.totalBytes }.isEqualTo(67108864000L)
            transform { it.availableBytes }.isEqualTo(45837607424L)
            transform { it.usedBytes }.isEqualTo(21271256576L)
            transform { it.percentageUsed }.isEqualTo(31)
            transform { it.errorMessage }.isEqualTo(null)
        }
    }

    @Test
    fun `should parse diskstats output with total and used bytes`() {
        val diskstatsOutput = """
            Storage Stats:
            Total bytes: 128000000000
            Used bytes: 96000000000
            Free bytes: 32000000000
        """.trimIndent()

        val result = command.parse(diskstatsOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isTrue()
            transform { it.totalBytes }.isEqualTo(128000000000L)
            transform { it.usedBytes }.isEqualTo(96000000000L)
            transform { it.availableBytes }.isEqualTo(32000000000L)
            transform { it.percentageUsed }.isEqualTo(75)
        }
    }

    @Test
    fun `should handle service unavailable - unknown service error`() {
        val errorOutput = "Unknown service: diskstats"

        val result = command.parse(errorOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isFalse()
            transform { it.totalBytes }.isEqualTo(0L)
            transform { it.usedBytes }.isEqualTo(0L)
            transform { it.availableBytes }.isEqualTo(0L)
            transform { it.percentageUsed }.isEqualTo(0)
            transform { it.errorMessage }.isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
        }
    }

    @Test
    fun `should handle service unavailable - can't find service error`() {
        val errorOutput = "Can't find service: diskstats"

        val result = command.parse(errorOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isFalse()
            transform { it.errorMessage }.isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
        }
    }

    @Test
    fun `should handle service unavailable - dumpsys not found`() {
        val errorOutput = "dumpsys: not found"

        val result = command.parse(errorOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isFalse()
            transform { it.errorMessage }.isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
        }
    }

    @Test
    fun `should handle empty output`() {
        val emptyOutput = ""

        val result = command.parse(emptyOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isFalse()
            transform { it.errorMessage }.isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
        }
    }

    @Test
    fun `should handle output with no parseable disk information`() {
        val unparsableOutput = """
            Some diskstats information
            but no actual storage bytes data
            other unrelated output
        """.trimIndent()

        val result = command.parse(unparsableOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isFalse()
            transform { it.errorMessage }.isEqualTo("No disk usage data found in diskstats output")
        }
    }

    @Test
    fun `should extract maximum bytes from lines with multiple numbers`() {
        val complexOutput = """
            Data partition info: 512 blocks, total: 134217728000 bytes, free: 67108864000 bytes
        """.trimIndent()

        val result = command.parse(complexOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isTrue()
            transform { it.totalBytes }.isEqualTo(134217728000L)
            transform { it.availableBytes }.isEqualTo(67108864000L)
            transform { it.usedBytes }.isEqualTo(67108864000L)
            transform { it.percentageUsed }.isEqualTo(50)
        }
    }

    @Test
    fun `should parse real diskstats output format with KB values`() {
        val realDiskstatsOutput = """
            Latency: 1ms [512B Data Write]
            Recent Disk Write Speed data unavailable
            Data-Free: 5108980K / 6082144K total = 83% free
            Cache-Free: 5108980K / 6082144K total = 83% free
            System-Free: 49384K / 808320K total = 6% free
            Metadata-Free: 10632K / 11248K total = 94% free
            File-based Encryption: true
        """.trimIndent()

        val result = command.parse(realDiskstatsOutput)

        assertThat(result).all {
            transform { it.isAvailable }.isTrue()
            transform { it.totalBytes }.isEqualTo(6228115456L)
            transform { it.availableBytes }.isEqualTo(5231595520L)
            transform { it.usedBytes }.isEqualTo(996519936L)
            transform { it.percentageUsed }.isEqualTo(16)
            transform { it.errorMessage }.isEqualTo(null)
        }
    }

    @Test
    fun `should handle diskstats output with various formatting`() {
        val variousFormats = """
            Data-Total: 50000000000 bytes
            System Free: 15000000000
            Cache-Used bytes: 5000000000
        """.trimIndent()

        val result = command.parse(variousFormats)

        assertThat(result).all {
            transform { it.isAvailable }.isTrue()
            transform { it.totalBytes }.isEqualTo(50000000000L)
            transform { it.availableBytes }.isEqualTo(15000000000L)
            transform { it.usedBytes }.isEqualTo(35000000000L)
            transform { it.percentageUsed }.isEqualTo(70)
        }
    }
}
