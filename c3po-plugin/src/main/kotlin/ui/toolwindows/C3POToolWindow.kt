package ui.toolwindows

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import kotlinx.coroutines.*
import models.AdbDevice
import services.PluginCommandExecutor
import ui.panels.ActivitiesPanel
import ui.panels.DevicePanel
import ui.panels.PackagesPanel
import javax.swing.*
import java.awt.BorderLayout

/**
 * Main tool window for C3PO Android Explorer.
 * Provides tabbed interface for device exploration with MVP features.
 */
class C3POToolWindow(private val project: Project) {

    companion object {
        private val LOG = Logger.getInstance(C3POToolWindow::class.java)
    }

    private val commandExecutor = PluginCommandExecutor(project)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var selectedDevice: AdbDevice? = null

    // UI Components
    private val devicePanel = DevicePanel(::onDeviceSelected, ::refreshDevices)
    private val activitiesPanel = ActivitiesPanel(commandExecutor)
    private val packagesPanel = PackagesPanel(commandExecutor)

    private val tabsComponent = JBTabbedPane()

    fun getContent(): JComponent {
        return createMainPanel()
    }

    private fun createMainPanel(): JPanel {
        val mainPanel = JPanel(BorderLayout())

        // Device selection at the top
        mainPanel.add(devicePanel.createPanel(), BorderLayout.NORTH)

        // Tabbed content in the center
        setupTabs()
        mainPanel.add(tabsComponent, BorderLayout.CENTER)

        // Initialize device list
        refreshDevices()

        return mainPanel
    }

    private fun setupTabs() {
        // Activities tab
        tabsComponent.addTab("Activities", JBScrollPane(activitiesPanel.createPanel()))

        // Packages tab
        tabsComponent.addTab("Packages", JBScrollPane(packagesPanel.createPanel()))
    }

    private fun onDeviceSelected(device: AdbDevice?) {
        selectedDevice = device
        LOG.info("Device selected: ${device?.name ?: "None"}")

        // Update tabs with new device
        device?.let {
            activitiesPanel.setDevice(it)
            packagesPanel.setDevice(it)

            // Refresh data for new device
            refreshCurrentTabData()
        }
    }

    private fun refreshDevices() {
        // Don't block UI initialization - launch safely
        try {
            scope.launch {
                try {
                    val devices = commandExecutor.getConnectedDevices()
                    SwingUtilities.invokeLater {
                        devicePanel.updateDevices(devices)
                    }
                } catch (e: Exception) {
                    LOG.error("Failed to refresh devices", e)
                    // Show empty device list on error
                    SwingUtilities.invokeLater {
                        devicePanel.updateDevices(emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error("Failed to launch device refresh coroutine", e)
            // Fallback: show empty device list
            SwingUtilities.invokeLater {
                devicePanel.updateDevices(emptyList())
            }
        }
    }

    private fun refreshCurrentTabData() {
        selectedDevice?.let { device ->
            try {
                scope.launch {
                    try {
                        // Refresh data based on current tab
                        when (tabsComponent.selectedIndex) {
                            0 -> activitiesPanel.refresh()  // Activities tab
                            1 -> packagesPanel.refresh()    // Packages tab
                        }
                    } catch (e: Exception) {
                        LOG.error("Failed to refresh tab data", e)
                    }
                }
            } catch (e: Exception) {
                LOG.error("Failed to launch tab refresh coroutine", e)
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
