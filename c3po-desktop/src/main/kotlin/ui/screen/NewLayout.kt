package ui.screen

import Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import core.App
import core.facade.PermissionChecker
import core.facade.update.UpdateUtils
import core.model.Action
import core.model.CommandStatus
import dev.amaro.sonic.IAction
import kotlinx.coroutines.delay
import plugins.Plugin
import ui.OnAction
import ui.component.DialogAction
import ui.component.SettingsDialog
import ui.component.StandardDialog
import ui.secondaryTextColor

@Composable
fun NewLayout(app: App) {
    val state = app.listen().collectAsState().value
    val onAction: OnAction = { action: IAction -> app.perform(action) }
    val selectedDevice = state.currentDevice?.id
    val selectedPlugin = state.currentPlugin
    val showLoading = state.commandStatus == CommandStatus.Running
    val errorMessage = state.errorMessage
    val successMessage = state.successMessage
    val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP) ?: ""
    val updatesUrl = state.settings.getProperty(Settings.UPDATES_URL_PROP) ?: ""
    val darkMode = state.settings.getProperty(Settings.DARK_MODE_PROP)?.toBoolean() ?: false
    val loggingEnabled = state.settings.getProperty(Settings.LOGGING_ENABLED_PROP)?.toBoolean() ?: false
    val analyticsEnabled = state.settings.getProperty(Settings.ANALYTICS_ENABLED_PROP)?.toBoolean() ?: false
    val adbLogging = state.settings.getProperty(Settings.LOGGING_ADB_PROP) ?: "full"
    val performLogging = state.settings.getProperty(Settings.LOGGING_PERFORM_PROP)?.toBoolean() ?: true
    val reduceLogging = state.settings.getProperty(Settings.LOGGING_REDUCE_PROP)?.toBoolean() ?: true
    val settingsState = state.settingsState

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showAnalyticsOptIn by remember { mutableStateOf(false) }

    // Auto-show settings dialog when ADB path is not configured
    LaunchedEffect(settingsState, adbPath) {
        if (settingsState == core.model.SettingsState.Initialized && adbPath.isBlank()) {
            showSettingsDialog = true
        }
    }

    // Show Analytics Opt-in dialog once when settings are loaded, not yet prompted and not enabled
    LaunchedEffect(settingsState) {
        if (settingsState == core.model.SettingsState.Initialized) {
            val enabled = state.settings.getProperty(Settings.ANALYTICS_ENABLED_PROP, "false").toBoolean()
            val prompted = state.settings.getProperty(Settings.ANALYTICS_OPTIN_PROMPTED_PROP, "false").toBoolean()
            if (!enabled && !prompted) {
                showAnalyticsOptIn = true
            }
        }
    }

    Row(Modifier.fillMaxSize()) {
        Sidebar(
            plugins = app.plugins,
            selectedPluginId = selectedPlugin,
            onPluginSelected = { onAction(Action.StartPlugin(it)) }
        )
        Column(Modifier.weight(1f).fillMaxHeight()) {
            TopBar(
                devices = state.devices,
                selectedDevice = selectedDevice,
                appVersion = state.appVersion,
                onDeviceSelected = { id ->
                    val device = state.devices.find { it.id == id }
                    if (device != null && settingsState == core.model.SettingsState.Initialized) {
                        onAction(Action.SelectDevice(device))
                        // Auto-select Device plugin when user manually selects a device
                        onAction(Action.StartPlugin("DEVICE"))
                    }
                },
                onRefreshDevices = {
                    if (settingsState == core.model.SettingsState.Initialized) onAction(Action.RefreshDevices)
                },
                onShowSettings = {
                    showSettingsDialog = true
                },
                onAction = onAction
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                ContentArea(
                    plugins = app.plugins,
                    selectedPluginId = selectedPlugin,
                    results = state.windows,
                    onAction = onAction
                )

                if (showLoading) LoadingPill()

                errorMessage?.let {
                    ErrorMessage(it) { onAction(Action.ClearError) }
                }

                successMessage?.let {
                    SuccessMessage(it) { onAction(Action.ClearSuccess) }
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            initialAdbPath = adbPath,
            initialUpdatesUrl = updatesUrl,
            initialDarkMode = darkMode,
            initialAnalyticsEnabled = analyticsEnabled,
            initialLoggingEnabled = loggingEnabled,
            initialAdbLogging = adbLogging,
            initialPerformLogging = performLogging,
            initialReduceLogging = reduceLogging,
            isSearchingAdbPath = state.isSearchingAdbPath,
            adbSearchError = state.adbSearchError,
            onAction = onAction,
            onSave = { newAdbPath, newUpdatesUrl, newDarkMode, newAnalyticsEnabled, newLoggingEnabled, newAdbLogging, newPerformLogging, newReduceLogging ->
                onAction(Action.ChangeSettingsProperty(Settings.ADB_PATH_PROP, newAdbPath))
                onAction(Action.ChangeSettingsProperty(Settings.UPDATES_URL_PROP, newUpdatesUrl))
                onAction(Action.ChangeSettingsProperty(Settings.DARK_MODE_PROP, newDarkMode.toString()))
                onAction(Action.ChangeSettingsProperty(Settings.ANALYTICS_ENABLED_PROP, newAnalyticsEnabled.toString()))
                onAction(Action.ChangeSettingsProperty(Settings.LOGGING_ENABLED_PROP, newLoggingEnabled.toString()))
                onAction(Action.ChangeSettingsProperty(Settings.LOGGING_ADB_PROP, newAdbLogging))
                onAction(Action.ChangeSettingsProperty(Settings.LOGGING_PERFORM_PROP, newPerformLogging.toString()))
                onAction(Action.ChangeSettingsProperty(Settings.LOGGING_REDUCE_PROP, newReduceLogging.toString()))
                showSettingsDialog = false
            },
            onCancel = { showSettingsDialog = false },
            onCheckPermissions = { showPermissionDialog = true }
        )
    }

    // Analytics Opt-in Dialog
    if (showAnalyticsOptIn && !showSettingsDialog) {
        ui.component.AnalyticsOptInDialog(
            onEnable = {
                onAction(Action.ChangeSettingsProperty(Settings.ANALYTICS_ENABLED_PROP, "true"))
                onAction(Action.ChangeSettingsProperty(Settings.ANALYTICS_OPTIN_PROMPTED_PROP, "true"))
                showAnalyticsOptIn = false
            },
            onDecline = {
                onAction(Action.ChangeSettingsProperty(Settings.ANALYTICS_OPTIN_PROMPTED_PROP, "true"))
                showAnalyticsOptIn = false
            },
            onDismiss = {
                // If user dismisses, do not mark as prompted to allow showing again
                showAnalyticsOptIn = false
            }
        )
    }

    // Permission Status Dialog (separate modal on top of Settings)
    if (showPermissionDialog) {
        val permissionStatus = remember { PermissionChecker.getPermissionStatus() }

        StandardDialog(
            title = "File Permission Status",
            onDismiss = { showPermissionDialog = false },
            primaryAction = DialogAction(
                text = "OK",
                onClick = { showPermissionDialog = false },
                isPrimary = true
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current Status:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                listOf(
                    "Documents" to permissionStatus.hasDocumentsAccess,
                    "Downloads" to permissionStatus.hasDownloadsAccess,
                    "Desktop" to permissionStatus.hasDesktopAccess
                ).forEach { (folderName, hasAccess) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasAccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            contentDescription = null,
                            tint = if (hasAccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$folderName folder: ${if (hasAccess) "Accessible" else "No access"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (permissionStatus.needsAttention) {
                    Text(
                        text = PermissionChecker.getPermissionInstructions(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = "All required file permissions are properly configured. File dialogs should work correctly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // Update notification dialog - only show when ADB is configured and settings dialog is not showing
    val showUpdateDialog = adbPath.isNotBlank() && !showSettingsDialog &&
            state.updateInfo != null && state.updateState in listOf(
        core.model.UpdateState.UpdateAvailable,
        core.model.UpdateState.Downloading,
        core.model.UpdateState.DownloadComplete,
        core.model.UpdateState.InstallReady,
        core.model.UpdateState.Installing,
        core.model.UpdateState.UpdateCancelled,
        core.model.UpdateState.Error
    )

    if (showUpdateDialog) {
        ui.component.UpdateNotificationDialog(
            updateInfo = state.updateInfo!!,
            updateState = state.updateState,
            downloadProgress = state.downloadProgress,
            installProgress = state.installProgress,
            errorMessage = state.errorMessage,
            onUpdateNow = {
                onAction(Action.DownloadUpdate(state.updateInfo!!))
            },
            onInstallNow = {
                // Get the downloaded file path from unified download directory
                val downloadDir = core.util.AppPaths.getUpdateDownloadDirectory()
                val downloadedFile =
                    java.io.File(downloadDir, UpdateUtils.getUpdateFileName(state.updateInfo!!.version))
                if (downloadedFile.exists()) {
                    onAction(Action.InstallUpdate(downloadedFile.absolutePath))
                } else {
                    onAction(Action.UpdateError("Downloaded file not found: ${downloadedFile.absolutePath}"))
                }
            },
            onUpdateLater = {
                onAction(Action.DismissUpdate) // Properly dismiss until next app restart
            },
            onRetry = {
                // Reset error state and retry the last operation
                when (state.updateState) {
                    core.model.UpdateState.Error -> {
                        // Determine what to retry based on available data
                        if (state.updateInfo != null) {
                            onAction(Action.DownloadUpdate(state.updateInfo!!))
                        } else {
                            onAction(Action.CheckForUpdate)
                        }
                    }

                    else -> {
                        onAction(Action.CheckForUpdate)
                    }
                }
            },
            onCancelDownload = {
                onAction(Action.CancelDownload)
            }
        )
    }

    // Restart confirmation dialog
    if (state.showRestartConfirmation) {
        StandardDialog(
            title = "Restart Device",
            onDismiss = { onAction(Action.DismissRestartConfirmation) },
            primaryAction = DialogAction(
                text = "Restart",
                onClick = { onAction(Action.RestartDevice) },
                isPrimary = true
            ),
            secondaryAction = DialogAction(
                text = "Cancel",
                onClick = { onAction(Action.DismissRestartConfirmation) }
            )
        ) {
            Text(
                text = "Are you sure you want to restart the device? This will temporarily disconnect the device and interrupt any running operations.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
private fun Sidebar(
    plugins: List<Plugin<*>>,
    selectedPluginId: String?,
    onPluginSelected: (String) -> Unit
) {
    Column(
        Modifier.width(120.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        plugins.forEach { plugin ->
            val isSelected = plugin.id == selectedPluginId
            val background =
                if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else Color.Transparent
            val internalPadding = if (isSelected) PaddingValues(horizontal = 0.dp, vertical = 4.dp) else PaddingValues(
                horizontal = 12.dp,
                vertical = 8.dp
            )
            Surface(
                onClick = { onPluginSelected(plugin.id) },
                shape = RoundedCornerShape(20.dp),
                color = background,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .height(72.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(internalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = plugin.icon,
                        contentDescription = plugin.name,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        plugin.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopBar(
    devices: List<core.model.AdbDevice>,
    selectedDevice: String?,
    appVersion: String,
    onDeviceSelected: (String) -> Unit,
    onRefreshDevices: () -> Unit,
    onShowSettings: () -> Unit,
    onAction: OnAction
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(24.dp))
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.height(40.dp)
                ) {
                    val selectedName = when {
                        devices.isEmpty() -> "No devices"
                        selectedDevice != null -> devices.find { it.id == selectedDevice }?.name
                            ?: ui.definitions.Texts.SELECT_DEVICE

                        else -> ui.definitions.Texts.SELECT_DEVICE
                    }
                    Text(selectedName, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Show devices",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    devices.forEach { device ->
                        DropdownMenuItem(
                            text = { Text(device.name) },
                            onClick = {
                                onDeviceSelected(device.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onRefreshDevices() }) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh")
            }
            Spacer(Modifier.weight(1f))


            // Help icon (opens Getting Started guide)
            IconButton(onClick = { onAction(Action.OpenUrl("https://amaro-dev.github.io/c3po/wiki/Getting-Started-Guide.html")) }) {
                Icon(
                    imageVector = Icons.Filled.Help,
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Settings gear icon
            IconButton(onClick = { onShowSettings() }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.width(8.dp))

            // Version display on the right
            Text(
                "v$appVersion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondaryTextColor
            )

            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun ContentArea(
    plugins: List<Plugin<*>>,
    selectedPluginId: String?,
    results: Map<String, core.model.WindowResult<*>>,
    onAction: OnAction
) {
    val selectedPlugin: Plugin<*>? = plugins.find { it.id == selectedPluginId }

    if (selectedPlugin != null) {
        (selectedPlugin as Plugin<*>?)?.render(results, onAction)
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No plugin selected", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun LoadingPill() {
    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(top = 24.dp, end = 32.dp)
                .wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 3.dp
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    "Processing...",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = MaterialTheme.colorScheme.error,
            shape = MaterialTheme.shapes.large,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(top = 24.dp, end = 32.dp)
                .wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    message,
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessMessage(message: String, onDismiss: () -> Unit) {
    // Auto-dismiss after 4 seconds using LaunchedEffect
    LaunchedEffect(message) {
        delay(4000)
        onDismiss()
    }
    
    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = Color(0xFF4CAF50), // Green success background
            shape = MaterialTheme.shapes.large,
            shadowElevation = 8.dp,
            modifier = Modifier
                .padding(top = 24.dp, end = 32.dp) // Same position as ErrorMessage
                .wrapContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Success",
                    tint = Color.White, // White icon on green background
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    message,
                    color = Color.White, // White text on green background
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
