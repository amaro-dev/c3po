import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NewLayout() {
    // State for UI-related elements
    val (selectedPlugin, setSelectedPlugin) = remember { mutableStateOf("") }
    val (selectedDevice, setSelectedDevice) = remember { mutableStateOf<String?>(null) }
    val (isCompanionConnected, setCompanionConnected) = remember { mutableStateOf(false) }
    val (showLoading, setShowLoading) = remember { mutableStateOf(false) }
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopBar(
                selectedDevice = selectedDevice,
                onDeviceSelected = setSelectedDevice,
                isCompanionConnected = isCompanionConnected,
                onRefreshDevices = { /* TODO: Implement refresh logic */ },
                onCompanionClick = { /* TODO: Open companion dialog */ }
            )
        },
        bottomBar = {},
        drawerContent = {
            Sidebar(
                selectedPlugin = selectedPlugin,
                onPluginSelected = setSelectedPlugin
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ContentArea(
                selectedPlugin = selectedPlugin,
                selectedDevice = selectedDevice
            )

            if (showLoading) {
                LoadingPill()
            }

            errorMessage?.let {
                ErrorMessage(
                    message = it,
                    onDismiss = { setErrorMessage(null) }
                )
            }
        }
    }
}

@Composable
fun TopBar(
    selectedDevice: String?,
    onDeviceSelected: (String?) -> Unit,
    isCompanionConnected: Boolean,
    onRefreshDevices: () -> Unit,
    onCompanionClick: () -> Unit
) {
    // TODO: Implement top bar UI
}

@Composable
fun Sidebar(
    selectedPlugin: String,
    onPluginSelected: (String) -> Unit
) {
    // TODO: Implement sidebar UI
}

@Composable
fun ContentArea(
    selectedPlugin: String,
    selectedDevice: String?
) {
    // TODO: Implement content area UI
}

@Composable
fun LoadingPill() {
    // TODO: Implement loading pill UI
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    // TODO: Implement error message UI
}
