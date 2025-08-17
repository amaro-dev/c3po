package ui

import Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import core.model.Action
import ui.definitions.Texts
import ui.definitions.Texts.Companion.ADB_PATH

@Composable
fun SettingsBox(onAction: (Action) -> Unit) {
    var adbPathValue by remember { mutableStateOf(Texts.EMPTY) }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colors.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth(0.5f),
        ) {
            TextField(
                adbPathValue,
                { adbPathValue = it },
                placeholder = { Text(ADB_PATH) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                { onAction(Action.ChangeSettingsProperty(Settings.ADB_PATH_PROP, adbPathValue)) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(Texts.SAVE)
            }
        }
    }
}
