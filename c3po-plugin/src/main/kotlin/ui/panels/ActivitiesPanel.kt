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
 * Panel displaying activities from the selected device.
 * MVP feature: Shows activities and allows launching them.
 */
class ActivitiesPanel(private val commandExecutor: PluginCommandExecutor) {

    companion object {
        private val LOG = Logger.getInstance(ActivitiesPanel::class.java)
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentDevice: AdbDevice? = null

    private val activitiesTableModel = ActivitiesTableModel()
    private val activitiesTable = JBTable(activitiesTableModel)
    private val statusLabel = JBLabel("Select a device to view activities")

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
        columnModel.getColumn(0).preferredWidth = 200 // Package
        columnModel.getColumn(1).preferredWidth = 300 // Activity
    }

    private fun createActionPanel(): JPanel {
        val panel = JPanel()

        val launchButton = JButton("Launch Activity")
        launchButton.addActionListener { launchSelectedActivity() }
        panel.add(launchButton)

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { refresh() }
        panel.add(refreshButton)

        return panel
    }

    fun setDevice(device: AdbDevice) {
        currentDevice = device
        statusLabel.text = "Loading activities from ${device.name}..."
        refresh()
    }

    fun refresh() {
        val device = currentDevice ?: return

        scope.launch {
            try {
                statusLabel.text = "Loading activities..."
                val activities = commandExecutor.listActivities(device)

                SwingUtilities.invokeLater {
                    activitiesTableModel.updateActivities(activities)
                    statusLabel.text = "Found ${activities.size} activities"
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
            val activity = activitiesTableModel.getActivityAt(selectedRow)
            val device = currentDevice

            if (device != null) {
                scope.launch {
                    try {
                        statusLabel.text = "Launching ${activity.packageName}/${activity.activityPath}..."
                        val success = commandExecutor.startActivity(device, activity.packageName, activity.activityPath)

                        SwingUtilities.invokeLater {
                            statusLabel.text = if (success) {
                                "Activity launched successfully"
                            } else {
                                "Failed to launch activity"
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
    }

    /**
     * Table model for activities
     */
    private class ActivitiesTableModel : AbstractTableModel() {
        private val activities = mutableListOf<ActivityInfo>()
        private val columnNames = arrayOf("Package", "Activity")

        override fun getRowCount(): Int = activities.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val activity = activities[rowIndex]
            return when (columnIndex) {
                0 -> activity.packageName
                1 -> activity.activityPath
                else -> ""
            }
        }

        fun updateActivities(newActivities: List<ActivityInfo>) {
            activities.clear()
            activities.addAll(newActivities)
            fireTableDataChanged()
        }

        fun getActivityAt(rowIndex: Int): ActivityInfo = activities[rowIndex]
    }
}
