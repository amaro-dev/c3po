package ui.panels

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import kotlinx.coroutines.*
import models.AdbDevice
import models.ActivityInfo
import services.PluginCommandExecutor
import javax.swing.*
import javax.swing.table.AbstractTableModel
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * Panel displaying activities from all connected devices.
 * Simplified to work with Android Studio's device management.
 */
class ActivitiesPanel(private val commandExecutor: PluginCommandExecutor) {

    companion object {
        private val LOG = Logger.getInstance(ActivitiesPanel::class.java)
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentDevices: List<AdbDevice> = emptyList()

    private val activitiesTableModel = ActivitiesTableModel()
    private val activitiesTable = JBTable(activitiesTableModel)
    private val statusLabel = JBLabel("Connect devices via Android Studio to view activities")

    fun createPanel(): JPanel {
        val panel = JPanel(BorderLayout())

        // Status label at top
        panel.add(statusLabel, BorderLayout.NORTH)

        // Activities table in center
        setupTable()
        panel.add(JBScrollPane(activitiesTable), BorderLayout.CENTER)

        // Action buttons at bottom
        panel.add(createActionPanel(), BorderLayout.SOUTH)

        return panel
    }

    private fun setupTable() {
        activitiesTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Double-click to launch activity
        activitiesTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    launchSelectedActivity()
                }
            }
        })

        // Set column widths
        val columnModel = activitiesTable.columnModel
        columnModel.getColumn(0).preferredWidth = 250 // Package
        columnModel.getColumn(1).preferredWidth = 350 // Activity
    }

    private fun createActionPanel(): JPanel {
        val panel = JPanel()

        val launchButton = JButton("Launch Activity")
        launchButton.addActionListener { launchSelectedActivity() }
        panel.add(launchButton)

        return panel
    }

    fun setDevices(devices: List<AdbDevice>) {
        currentDevices = devices
        if (devices.isEmpty()) {
            statusLabel.text = "No devices connected"
            SwingUtilities.invokeLater {
                activitiesTableModel.updateActivities(emptyList())
            }
        } else {
            statusLabel.text = "Loading activities..."
            refresh()
        }
    }

    fun refresh() {
        scope.launch {
            try {
                SwingUtilities.invokeLater {
                    statusLabel.text = "Loading activities..."
                }

                // Get current devices
                val devices = commandExecutor.getConnectedDevices()
                currentDevices = devices.take(1) // Take only first device

                if (currentDevices.isEmpty()) {
                    SwingUtilities.invokeLater {
                        statusLabel.text = "No devices connected"
                        activitiesTableModel.updateActivities(emptyList())
                    }
                    return@launch
                }

                val allActivities = mutableListOf<ActivityWithDevice>()

                // Load activities from first device only
                val device = currentDevices.first()
                try {
                    val activities = commandExecutor.listActivities(device)
                    val activitiesWithDevice = activities.map { ActivityWithDevice(device, it) }
                    allActivities.addAll(activitiesWithDevice)
                } catch (e: Exception) {
                    LOG.error("Failed to load activities from device ${device.name}", e)
                }

                SwingUtilities.invokeLater {
                    activitiesTableModel.updateActivities(allActivities)
                    statusLabel.text = "Found ${allActivities.size} activities"
                }
            } catch (e: Exception) {
                LOG.error("Failed to load activities", e)
                SwingUtilities.invokeLater {
                    statusLabel.text = "Error loading activities: ${e.message}"
                }
            }
        }
    }

    private fun launchSelectedActivity() {
        val selectedRow = activitiesTable.selectedRow
        if (selectedRow >= 0) {
            val activityWithDevice = activitiesTableModel.getActivityAt(selectedRow)
            val device = activityWithDevice.device
            val activity = activityWithDevice.activity

            scope.launch {
                try {
                    SwingUtilities.invokeLater {
                        statusLabel.text =
                            "Launching ${activity.packageName}/${activity.activityPath} on ${device.name}..."
                    }

                    val success = commandExecutor.startActivity(device, activity.packageName, activity.activityPath)

                    SwingUtilities.invokeLater {
                        statusLabel.text = if (success) {
                            "Activity launched successfully on ${device.name}"
                        } else {
                            "Failed to launch activity on ${device.name}"
                        }
                    }
                } catch (e: Exception) {
                    LOG.error("Failed to launch activity", e)
                    SwingUtilities.invokeLater {
                        statusLabel.text = "Error launching activity: ${e.message}"
                    }
                }
            }
        }
    }

    /**
     * Data class to hold activity with its associated device
     */
    private data class ActivityWithDevice(
        val device: AdbDevice,
        val activity: ActivityInfo
    )

    /**
     * Table model for activities with device information
     */
    private class ActivitiesTableModel : AbstractTableModel() {
        private val activities = mutableListOf<ActivityWithDevice>()
        private val columnNames = arrayOf("Package", "Activity")

        override fun getRowCount(): Int = activities.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val activityWithDevice = activities[rowIndex]
            return when (columnIndex) {
                0 -> activityWithDevice.activity.packageName
                1 -> activityWithDevice.activity.activityPath
                else -> ""
            }
        }

        fun updateActivities(newActivities: List<ActivityWithDevice>) {
            activities.clear()
            activities.addAll(newActivities)
            fireTableDataChanged()
        }

        fun getActivityAt(rowIndex: Int): ActivityWithDevice = activities[rowIndex]
    }
}
