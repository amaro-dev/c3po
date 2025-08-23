import assertk.assertThat
import assertk.assertions.isEqualTo
import core.model.UpdateState
import org.junit.jupiter.api.Test

class UpdateStateTest {
    @Test
    fun `should have all required states`() {
        val expectedStates = setOf(
            "NoUpdate",
            "CheckingForUpdate",
            "UpdateAvailable",
            "Downloading",
            "DownloadComplete",
            "Error"
        )

        val actualStates = UpdateState.entries.map { it.name }.toSet()

        assertThat(actualStates).isEqualTo(expectedStates)
    }
}