package core.command

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
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

        assertThat(result).all {
            // Device info (from getprop only)
            transform { it.device.model }.isEqualTo("Pixel 6")
            transform { it.device.brand }.isEqualTo("Google")
            transform { it.device.processor.trim() }.isEqualTo("tensor")
            transform { it.device.architecture.trim() }.isEqualTo("arm64-v8a")
            transform { it.device.serialNumber }.isEqualTo("1A2B3C4D5E6F")
            transform { it.device.ramSize }.isEqualTo("Unknown")
            transform { it.device.screenSize }.isEqualTo("Unknown")
            transform { it.device.screenResolution }.isEqualTo("Unknown DPI")

            // System info
            transform { it.system.androidVersion }.isEqualTo("14")
            transform { it.system.securityPatch }.isEqualTo("2024-01-01")
            transform { it.system.build }.isEqualTo("UQ1A.240105.004")
            transform { it.system.firmware }.isEqualTo("google/oriole/oriole:14/UQ1A.240105.004/11206848:user/release-keys")

            // Status info (basic from getprop)
            transform { it.status.batteryLevel }.isEqualTo(0)
            transform { it.status.batteryHealth }.isEqualTo("Unknown")
            transform { it.status.batteryTemperature }.isEqualTo(0f)
            transform { it.status.chargingStatus }.isEqualTo("Unknown")
            transform { it.status.batteryVoltage }.isEqualTo(0)
            transform { it.status.connectionMode }.isEqualTo("Wi-Fi")
            transform { it.status.connectionDetails }.isEqualTo("wlan0")
            transform { it.status.diskUsage }.isNotNull()
            transform { it.status.diskUsage.isAvailable }.isFalse()
        }
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

        assertThat(result).all {
            transform { it.status.connectionMode }.isEqualTo("Mobile")
            transform { it.status.connectionDetails }.isEqualTo(null)
        }
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

        assertThat(result).all {
            transform { it.status.connectionMode }.isEqualTo("Unknown")
            transform { it.status.connectionDetails }.isEqualTo(null)
        }
    }

    @Test
    fun `should handle missing properties gracefully`() {
        val minimalOutput = """
            [ro.product.model]: []
            [ro.product.brand]: []
        """.trimIndent()

        val result = command.parse(minimalOutput)

        // Should provide defaults for missing data
        assertThat(result).all {
            transform { it.device.model }.isEqualTo("Unknown")
            transform { it.device.brand }.isEqualTo("Unknown")
            transform { it.device.processor }.isEqualTo("Unknown")
            transform { it.system.androidVersion }.isEqualTo("Unknown")
            transform { it.status.connectionMode }.isEqualTo("Unknown")
        }
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

        assertThat(result).all {
            transform { it.device.model }.isEqualTo("Test Device")
            transform { it.device.brand }.isEqualTo("Unknown")
            transform { it.device.processor }.isEqualTo("Unknown")
            transform { it.system.androidVersion }.isEqualTo("Unknown")
        }
    }
}
