package plugins.packages.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.model.Action
import core.model.AppPackage
import core.model.SleepState
import plugins.packages.definition.PackagesPlugin
import plugins.packages.ui.SignatureCard
import ui.OnAction
import ui.component.CustomActionButton

/**
 * Enhanced package row component for the packages list
 */
@Composable
fun EnhancedPackageRow(
    packageInfo: AppPackage,
    onAction: OnAction,
    showBottomBorder: Boolean = true,
) {
    Column {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Package info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = packageInfo.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${packageInfo.versionName} (${packageInfo.versionCode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Status indicators on same line as version
                        if (packageInfo.isSystemApp) {
                            Text(
                                text = " • System",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                        if (packageInfo.isDebuggable) {
                            Text(
                                text = " • Debug",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                        if (!packageInfo.isEnabled) {
                            Text(
                                text = " • Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Copy button
                    CustomActionButton(
                        icon = Icons.Outlined.FileCopy,
                        contentDescription = "Copy package name",
                        onClick = { onAction(Action.CopyText(packageInfo.packageName)) }
                    )

                    // Sleep state button
                    val sleepIcon = when (packageInfo.sleepState) {
                        SleepState.Unknown -> Icons.Filled.HelpOutline
                        SleepState.Awake -> Icons.Filled.Visibility
                        SleepState.Asleep -> Icons.Filled.VisibilityOff
                    }
                    CustomActionButton(
                        icon = sleepIcon,
                        contentDescription = "Sleep state",
                        onClick = {
                            if (packageInfo.sleepState == SleepState.Unknown) {
                                onAction(PackagesPlugin.Actions.CheckAsleep(packageInfo))
                            }
                        }
                    )

                    // Extract key button
                    CustomActionButton(
                        icon = Icons.Filled.Key,
                        contentDescription = "Extract key",
                        onClick = { onAction(PackagesPlugin.Actions.ExtractKey(packageInfo)) }
                    )

                    // Stop button
                    CustomActionButton(
                        icon = Icons.Filled.Stop,
                        contentDescription = "Stop",
                        onClick = { onAction(PackagesPlugin.Actions.Stop(packageInfo)) }
                    )

                    // Clear data button
                    CustomActionButton(
                        icon = Icons.Filled.CleaningServices,
                        contentDescription = "Clear data",
                        onClick = { onAction(PackagesPlugin.Actions.ClearData(packageInfo)) }
                    )

                    // Uninstall button
                    CustomActionButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = "Uninstall",
                        onClick = { onAction(PackagesPlugin.Actions.Uninstall(packageInfo)) }
                    )
                }
            }
        }

        // Signature card if available
        packageInfo.signerInfo?.let { signerInfo ->
            SignatureCard(signerInfo, onAction)
        }

        // Bottom border
        if (showBottomBorder) {
            Divider(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}