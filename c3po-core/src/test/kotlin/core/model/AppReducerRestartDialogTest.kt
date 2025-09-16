package core.model

import assertk.all
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class AppReducerRestartDialogTest {

    private val reducer = AppReducer()
    private val initialState = AppState()

    @Test
    fun `ConfirmRestartDevice sets showRestartConfirmation to true`() {
        // Given
        val initialState = AppState(showRestartConfirmation = false)

        // When
        val result = reducer.reduce(Action.ConfirmRestartDevice, initialState)

        // Then
        assertThat(result.showRestartConfirmation).isTrue()
    }

    @Test
    fun `DismissRestartConfirmation sets showRestartConfirmation to false`() {
        // Given
        val stateWithDialogShown = AppState(showRestartConfirmation = true)

        // When
        val result = reducer.reduce(Action.DismissRestartConfirmation, stateWithDialogShown)

        // Then
        assertThat(result.showRestartConfirmation).isFalse()
    }

    @Test
    fun `confirmation dialog state is preserved with other state changes`() {
        // Given
        val stateWithDialog = AppState(
            showRestartConfirmation = true,
            errorMessage = "Some error"
        )

        // When - Another action that doesn't affect dialog state
        val result = reducer.reduce(Action.ClearError, stateWithDialog)

        // Then - Dialog state is preserved, error is cleared
        assertThat(result).all {
            transform { it.showRestartConfirmation }.isTrue()
            transform { it.errorMessage }.isNull()
        }
    }

    @Test
    fun `multiple dialog state changes work correctly`() {
        // Start with dialog hidden
        var state = AppState(showRestartConfirmation = false)

        // Show dialog
        state = reducer.reduce(Action.ConfirmRestartDevice, state)
        assertThat(state.showRestartConfirmation).isTrue()

        // Hide dialog
        state = reducer.reduce(Action.DismissRestartConfirmation, state)
        assertThat(state.showRestartConfirmation).isFalse()

        // Show dialog again
        state = reducer.reduce(Action.ConfirmRestartDevice, state)
        assertThat(state.showRestartConfirmation).isTrue()
    }
}