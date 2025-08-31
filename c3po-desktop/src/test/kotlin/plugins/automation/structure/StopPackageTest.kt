package plugins.automation.structure

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class StopPackageTest {

    @Test
    fun `ScriptStepType STOP_PACKAGE has correct display name`() {
        assertThat(ScriptStepType.STOP_PACKAGE.displayName).isEqualTo("Stop Package")
    }

    @Test
    fun `ScriptStep StopPackage has correct type`() {
        val stopPackageStep = ScriptStep.StopPackage("com.example.app")
        assertThat(stopPackageStep.type).isEqualTo("stop_package")
        assertThat(stopPackageStep.packageName).isEqualTo("com.example.app")
    }
}