package ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.overlayColor

data class DialogAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val isPrimary: Boolean = false
)

@Composable
fun StandardDialog(
    title: String,
    onDismiss: () -> Unit,
    primaryAction: DialogAction? = null,
    secondaryAction: DialogAction? = null,
    width: Int = 600,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.overlayColor),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(width.dp)
        ) {
            Column(
                Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                // Content
                content()

                // Action buttons (if any provided)
                if (primaryAction != null || secondaryAction != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        // Secondary button first (Cancel-style)
                        secondaryAction?.let { action ->
                            SecondaryButton(
                                text = action.text,
                                onClick = action.onClick,
                                enabled = action.enabled
                            )
                        }

                        // Primary button second (Save-style)
                        primaryAction?.let { action ->
                            PrimaryButton(
                                text = action.text,
                                onClick = action.onClick,
                                enabled = action.enabled
                            )
                        }
                    }
                }
            }
        }
    }
}