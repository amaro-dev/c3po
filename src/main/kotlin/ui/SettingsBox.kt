package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import core.Action
import ui.Texts.Companion.ADB_PATH


@Composable
fun SettingsBox(onAction: (Action) -> Unit) {
    var adbPathValue by remember { mutableStateOf(Texts.EMPTY) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth(0.5f),
        ) {
            TextField(
                adbPathValue,
                { adbPathValue = it },
                placeholder = { Text(ADB_PATH) },
                modifier = Modifier.fillMaxWidth()
            )
            Button({ onAction(Action.SaveSettings(adbPathValue)) }, modifier = Modifier.align(Alignment.End)) {
                Text(Texts.SAVE)
            }
        }
    }
}
