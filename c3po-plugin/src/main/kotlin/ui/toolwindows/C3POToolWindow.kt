package ui.toolwindows

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import kotlinx.coroutines.*
import services.PluginCommandExecutor
import ui.panels.ActivitiesPanel
import ui.panels.PackagesPanel
import java.awt.BorderLayout
import javax.swing.*

/**
 * Main tool window for C3PO Android Explorer.
 * Simplified to work with Android Studio's built-in device management.
 * No custom device selector - relies on Android Studio's device infrastructure.
 */
class C3POToolWindow(
    private val project: Project,
) {
    companion object {
        private val LOG = Logger.getInstance(C3POToolWindow::class.java)
    }

    private val commandExecutor = PluginCommandExecutor(project)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // UI Components - simplified without device panel
    private val activitiesPanel = ActivitiesPanel(commandExecutor)
    private val packagesPanel = PackagesPanel(commandExecutor)
    private val tabsComponent = JBTabbedPane()
    private val debugLabel = JLabel("Debug: Initializing...")

    // Track currently displayed device
    private var selectedDeviceId: String? = null
    private var monitorJob: Job? = null

    fun getContent(): JComponent = createMainPanel()

    private fun createMainPanel(): JPanel {
        val mainPanel = JPanel(BorderLayout())

        // Info panel at the top explaining device usage
        val infoPanel = createInfoPanel()
        mainPanel.add(infoPanel, BorderLayout.NORTH)

        // Debug label for troubleshooting
        mainPanel.add(debugLabel, BorderLayout.SOUTH)

        // Tabbed content in the center
        setupTabs()
        mainPanel.add(tabsComponent, BorderLayout.CENTER)

        // Initialize data for selected device
        refreshAllData()

        // Start background job to watch for device selection changes every 2 seconds
        monitorJob =
            scope.launch {
                while (isActive) {
                    try {
                        val devices = commandExecutor.getConnectedDevices()
                        val currentId = devices.firstOrNull()?.id
                        if (currentId != selectedDeviceId) {
                            SwingUtilities.invokeLater {
                                debugLabel.text = "Debug: Device changed, refreshing..."
                            }
                            refreshAllData()
                        }
                    } catch (e: Exception) {
                        LOG.warn("Device monitor error", e)
                    }
                    delay(2000)
                }
            }

        return mainPanel
    }

    private fun createInfoPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val infoLabel =
            JLabel("<html><b>C3PO Android Explorer</b> - Works with all connected devices via Android Studio's device management</html>")
        infoLabel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        panel.add(infoLabel, BorderLayout.CENTER)

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener {
            // Direct refresh of current tab
            when (tabsComponent.selectedIndex) {
                0 -> activitiesPanel.refresh()
                1 -> packagesPanel.refresh()
            }
        }
        panel.add(refreshButton, BorderLayout.EAST)

        return panel
    }

    private fun setupTabs() {
        // Activities tab
        tabsComponent.addTab("Activities", JBScrollPane(activitiesPanel.createPanel()))

        // Packages tab
        tabsComponent.addTab("Packages", JBScrollPane(packagesPanel.createPanel()))
    }

    private fun refreshAllData() {
        try {
            LOG.info("=== Starting device refresh ===")
            SwingUtilities.invokeLater {
                debugLabel.text = "Debug: Starting device refresh..."
            }

            scope.launch {
                try {
                    LOG.info("Getting connected devices...")
                    SwingUtilities.invokeLater {
                        debugLabel.text = "Debug: Getting connected devices..."
                    }

                    val devices = commandExecutor.getConnectedDevices()
                    LOG.info("Found ${devices.size} connected devices: ${devices.map { "${it.name} (${it.id})" }}")

                    SwingUtilities.invokeLater {
                        debugLabel.text = "Debug: Found ${devices.size} devices: ${
                            devices.map { it.name }.joinToString(", ").takeIf { it.isNotEmpty() } ?: "None"
                        }"

                        // Use only the first connected device to match Android Studio's selected device
                        val selectedDevices = devices.take(1)
                        LOG.info("Updating panels with selected device list: ${selectedDevices.map { it.name }}")

                        activitiesPanel.setDevices(selectedDevices)
                        packagesPanel.setDevices(selectedDevices)

                        // Refresh data for current tab
                        LOG.info("Refreshing current tab data...")
                        refreshCurrentTabData()

                        selectedDeviceId = selectedDevices.firstOrNull()?.id
                    }
                } catch (e: Exception) {
                    LOG.error("Failed to refresh devices", e)
                    SwingUtilities.invokeLater {
                        debugLabel.text = "Debug: Error getting devices: ${e.message}"
                        // Show empty data on error
                        activitiesPanel.setDevices(emptyList())
                        packagesPanel.setDevices(emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error("Failed to launch device refresh coroutine", e)
            SwingUtilities.invokeLater {
                debugLabel.text = "Debug: Failed to start refresh: ${e.message}"
            }
        }
    }

    private fun refreshCurrentTabData() {
        when (tabsComponent.selectedIndex) {
            0 -> activitiesPanel.refresh()
            1 -> packagesPanel.refresh()
        }
    }

    fun dispose() {
        scope.cancel()
        monitorJob?.cancel()
    }
}
