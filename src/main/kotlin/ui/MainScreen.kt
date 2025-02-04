package ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import core.Action
import core.App
import core.SettingsState
import ui.plugins.Plugin

@Composable
fun MainScreen(app: App, sideSection: Section) {
    val state = app.listen().collectAsState().value
    var size by remember { mutableStateOf(Size.Zero) }
    Box(
        Modifier.fillMaxSize()
            .onGloballyPositioned { coordinates ->
                size = coordinates.size.toSize()
            }
    ) {
        Row(Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colors.surface,
                modifier = Modifier
                    .width(275.dp)
                    .fillMaxHeight()
            ) {
                Column(Modifier.fillMaxSize()) {
                    sideSection(state) { app.perform(it) }
                }
            }
            Spacer(Modifier.background(MaterialTheme.colors.primary).width(4.dp).fillMaxHeight())
            Surface(
                color = MaterialTheme.colors.background,
                modifier = Modifier.weight(1f).defaultMinSize(450.dp).fillMaxHeight()
            ) {
                Column(Modifier.fillMaxSize()) {
                    state.currentPlugin?.let { name ->
                        val selectedPlugin: Plugin<*>? = app.plugins.find { it.id == name }
                        selectedPlugin?.render(state.windows) { app.perform(it) }
                    }
                }
                if (state.settingsState == SettingsState.NotFound) {
                    SettingsBox { app.perform(it) }
                } else {
                    // Forcing the use of companion app for now
//                    if (state.shouldShowCompanionDialog) {
//                        CompanionDialog(!state.settings.hasSelectedCompanionOption()) { app.perform(it) }
//                    }
                }
            }

        }
        Feedback(state.commandStatus, state.errorMessage) { app.perform(Action.ClearError) }
    }
}
