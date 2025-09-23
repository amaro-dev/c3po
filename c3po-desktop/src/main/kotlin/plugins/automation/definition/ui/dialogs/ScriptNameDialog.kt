package plugins.automation.definition.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ui.component.CustomTextField
import ui.component.DialogAction
import ui.component.StandardDialog

@Composable
fun ScriptNameDialog(
    isRename: Boolean,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    StandardDialog(
        title = if (isRename) "Rename Script" else "New Script",
        onDismiss = onDismiss,
        primaryAction = DialogAction(
            text = if (isRename) "Rename" else "Create",
            onClick = { onConfirm(name) },
            isPrimary = true,
        ),
        secondaryAction = DialogAction(
            text = "Cancel",
            onClick = onDismiss
        )
    ) {
        CustomTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Enter script name...",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

