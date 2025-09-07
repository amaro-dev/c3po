package core.command

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

        assertThat(result.isAvailable).isTrue()
        assertThat(result.totalBytes).isEqualTo(67108864000L)
        assertThat(result.availableBytes).isEqualTo(45837607424L) // Data-Free + System-Free
        assertThat(result.usedBytes).isEqualTo(21271256576L) // Total - Available
        assertThat(result.percentageUsed).isEqualTo(31) // ~31%
        assertThat(result.errorMessage).isEqualTo(null)
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

        assertThat(result.isAvailable).isTrue()
        assertThat(result.totalBytes).isEqualTo(128000000000L)
        assertThat(result.usedBytes).isEqualTo(96000000000L)
        assertThat(result.availableBytes).isEqualTo(32000000000L)
        assertThat(result.percentageUsed).isEqualTo(75) // 75%
    }

    @Test
    fun `should handle service unavailable - unknown service error`() {
        val errorOutput = "Unknown service: diskstats"

        val result = command.parse(errorOutput)

        assertThat(result.isAvailable).isFalse()
        assertThat(result.totalBytes).isEqualTo(0L)
        assertThat(result.usedBytes).isEqualTo(0L)
        assertThat(result.availableBytes).isEqualTo(0L)
        assertThat(result.percentageUsed).isEqualTo(0)
        assertThat(result.errorMessage).isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
    }

    @Test
    fun `should handle service unavailable - can't find service error`() {
        val errorOutput = "Can't find service: diskstats"

        val result = command.parse(errorOutput)

        assertThat(result.isAvailable).isFalse()
        assertThat(result.errorMessage).isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
    }

    @Test
    fun `should handle service unavailable - dumpsys not found`() {
        val errorOutput = "dumpsys: not found"

        val result = command.parse(errorOutput)

        assertThat(result.isAvailable).isFalse()
        assertThat(result.errorMessage).isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
    }

    @Test
    fun `should handle empty output`() {
        val emptyOutput = ""

        val result = command.parse(emptyOutput)

        assertThat(result.isAvailable).isFalse()
        assertThat(result.errorMessage).isEqualTo("Detailed disk stats are unavailable on this device (the `diskstats` dumpsys service was not found).")
    }

    @Test
    fun `should handle output with no parseable disk information`() {
        val unparsableOutput = """
            Some diskstats information
            but no actual storage bytes data
            other unrelated output
        """.trimIndent()

        val result = command.parse(unparsableOutput)

        assertThat(result.isAvailable).isFalse()
        assertThat(result.errorMessage).isEqualTo("No disk usage data found in diskstats output")
    }

    @Test
    fun `should extract maximum bytes from lines with multiple numbers`() {
        val complexOutput = """
            Data partition info: 512 blocks, total: 134217728000 bytes, free: 67108864000 bytes
        """.trimIndent()

        val result = command.parse(complexOutput)

        assertThat(result.isAvailable).isTrue()
        assertThat(result.totalBytes).isEqualTo(134217728000L)
        assertThat(result.availableBytes).isEqualTo(67108864000L)
        assertThat(result.usedBytes).isEqualTo(67108864000L) // 134217728000 - 67108864000
        assertThat(result.percentageUsed).isEqualTo(50) // 50%
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

        assertThat(result.isAvailable).isTrue()
        // Total should be Data partition total: 6082144K
        assertThat(result.totalBytes).isEqualTo(6228115456L) // 6082144K * 1024  
        // Available should be Data partition free: 5108980K
        assertThat(result.availableBytes).isEqualTo(5231595520L) // 5108980K * 1024
        // Used should be total - available
        assertThat(result.usedBytes).isEqualTo(996519936L)
        assertThat(result.percentageUsed).isEqualTo(16) // 996519936/6228115456 = ~16%
        assertThat(result.errorMessage).isEqualTo(null)
    }

    @Test
    fun `should handle diskstats output with various formatting`() {
        val variousFormats = """
            Data-Total: 50000000000 bytes
            System Free: 15000000000
            Cache-Used bytes: 5000000000
        """.trimIndent()

        val result = command.parse(variousFormats)

        assertThat(result.isAvailable).isTrue()
        assertThat(result.totalBytes).isEqualTo(50000000000L)
        assertThat(result.availableBytes).isEqualTo(15000000000L) // System Free
        assertThat(result.usedBytes).isEqualTo(35000000000L) // Total - Available  
        assertThat(result.percentageUsed).isEqualTo(70) // 70%
    }
}