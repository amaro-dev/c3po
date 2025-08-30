package core.command

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class GetFullDeviceInfoCommandTest {

    private val command = GetFullDeviceInfoCommand()

    @Test
    fun `should parse complete device info from real ADB output`() {
        val realAdbOutput = """
            [ro.product.model]: [Pixel 6]
            [ro.product.brand]: [Google]
            [ro.hardware]: [tensor]
            [ro.product.cpu.abi]: [arm64-v8a]
            [ro.serialno]: [1A2B3C4D5E6F]
            [ro.build.version.release]: [14]
            [ro.build.version.security_patch]: [2024-01-01]
            [ro.build.display.id]: [UQ1A.240105.004]
            [ro.build.fingerprint]: [google/oriole/oriole:14/UQ1A.240105.004/11206848:user/release-keys]
            [wifi.interface]: [wlan0]
        """.trimIndent()

        val result = command.parse(realAdbOutput)

        // Verify Device info (from getprop only)
        assertThat(result.device.model).isEqualTo("Pixel 6")
        assertThat(result.device.brand).isEqualTo("Google")
        assertThat(result.device.processor.trim()).isEqualTo("tensor")
        assertThat(result.device.architecture.trim()).isEqualTo("arm64-v8a")
        assertThat(result.device.serialNumber).isEqualTo("1A2B3C4D5E6F")
        assertThat(result.device.ramSize).isEqualTo("Unknown") // Requires separate command
        assertThat(result.device.screenSize).isEqualTo("Unknown") // Requires separate command
        assertThat(result.device.screenResolution).isEqualTo("Unknown DPI") // Requires separate command

        // Verify System info
        assertThat(result.system.androidVersion).isEqualTo("14")
        assertThat(result.system.securityPatch).isEqualTo("2024-01-01")
        assertThat(result.system.build).isEqualTo("UQ1A.240105.004")
        assertThat(result.system.firmware).isEqualTo("google/oriole/oriole:14/UQ1A.240105.004/11206848:user/release-keys")

        // Verify Status info (basic from getprop)
        assertThat(result.status.batteryLevel).isEqualTo(0) // Requires separate command
        assertThat(result.status.batteryHealth).isEqualTo("Unknown") // Requires separate command
        assertThat(result.status.batteryTemperature).isEqualTo(0f) // Requires separate command
        assertThat(result.status.chargingStatus).isEqualTo("Unknown") // Requires separate command
        assertThat(result.status.batteryVoltage).isEqualTo(0) // Requires separate command
        assertThat(result.status.connectionMode).isEqualTo("Wi-Fi")
        assertThat(result.status.connectionDetails).isEqualTo("wlan0")
        assertThat(result.status.diskUsage).isEmpty() // Requires separate command
    }

    @Test
    fun `should handle mobile connection`() {
        val mobileOutput = """
            [ro.product.model]: [Galaxy S23]
            [ro.product.brand]: [Samsung]
            [ro.hardware]: [qcom]
            [ro.product.cpu.abi]: [arm64-v8a]
            [ro.serialno]: [ABC123XYZ]
            [ro.build.version.release]: [13]
            [ro.build.version.security_patch]: [2023-12-01]
            [ro.build.display.id]: [TP1A.220624.014]
            [ro.build.fingerprint]: [samsung/dm1q/dm1q:13/TP1A.220624.014/S908BXXU2AWL2:user/release-keys]
            [ro.telephony.default_network]: [22,22]
        """.trimIndent()

        val result = command.parse(mobileOutput)

        assertThat(result.status.connectionMode).isEqualTo("Mobile")
        assertThat(result.status.connectionDetails).isEqualTo(null)
    }

    @Test
    fun `should handle unknown connection`() {
        val unknownOutput = """
            [ro.product.model]: [Test Device]
            [ro.product.brand]: [Test Brand]
            [ro.hardware]: [test]
            [ro.product.cpu.abi]: [arm64-v8a]
            [ro.serialno]: [TEST123]
            [ro.build.version.release]: [12]
            [ro.build.version.security_patch]: [2023-01-01]
            [ro.build.display.id]: [TEST.BUILD]
            [ro.build.fingerprint]: [test/test/test:12/TEST.BUILD/123:user/release-keys]
        """.trimIndent()

        val result = command.parse(unknownOutput)

        assertThat(result.status.connectionMode).isEqualTo("Unknown")
        assertThat(result.status.connectionDetails).isEqualTo(null)
    }

    @Test
    fun `should handle missing properties gracefully`() {
        val minimalOutput = """
            [ro.product.model]: []
            [ro.product.brand]: []
        """.trimIndent()

        val result = command.parse(minimalOutput)

        // Should provide defaults for missing data
        assertThat(result.device.model).isEqualTo("Unknown")
        assertThat(result.device.brand).isEqualTo("Unknown")
        assertThat(result.device.processor).isEqualTo("Unknown")
        assertThat(result.system.androidVersion).isEqualTo("Unknown")
        assertThat(result.status.connectionMode).isEqualTo("Unknown")
    }

    @Test
    fun `should handle empty property values`() {
        val emptyValuesOutput = """
            [ro.product.model]: [Test Device]
            [ro.product.brand]: []
            [ro.hardware]: []
            [ro.build.version.release]: []
        """.trimIndent()

        val result = command.parse(emptyValuesOutput)

        assertThat(result.device.model).isEqualTo("Test Device")
        assertThat(result.device.brand).isEqualTo("Unknown")
        assertThat(result.device.processor).isEqualTo("Unknown")
        assertThat(result.system.androidVersion).isEqualTo("Unknown")
    }
}