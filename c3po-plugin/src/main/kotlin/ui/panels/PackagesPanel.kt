package ui.panels

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import kotlinx.coroutines.*
import models.AdbDevice
import models.AppPackage
import services.PluginCommandExecutor
import javax.swing.*
import javax.swing.table.AbstractTableModel
import java.awt.BorderLayout

/**
 * Panel displaying installed packages from the selected device.
 * MVP feature: Shows installed applications and their information.
 */
class PackagesPanel(private val commandExecutor: PluginCommandExecutor) {

    companion object {
        private val LOG = Logger.getInstance(PackagesPanel::class.java)
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentDevice: AdbDevice? = null

    private val packagesTableModel = PackagesTableModel()
    private val packagesTable = JBTable(packagesTableModel)
    private val statusLabel = JBLabel("Select a device to view packages")

    fun createPanel(): JPanel {
        val panel = JPanel(BorderLayout())

        // Status label at top
        panel.add(statusLabel, BorderLayout.NORTH)

        // Packages table in center
        setupTable()
        panel.add(JBScrollPane(packagesTable), BorderLayout.CENTER)

        // Action buttons at bottom
        panel.add(createActionPanel(), BorderLayout.SOUTH)

        return panel
    }

    private fun setupTable() {
        packagesTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Set column widths
        val columnModel = packagesTable.columnModel
        columnModel.getColumn(0).preferredWidth = 300 // Package Name
        columnModel.getColumn(1).preferredWidth = 100 // Version Name
        columnModel.getColumn(2).preferredWidth = 80  // Version Code
        columnModel.getColumn(3).preferredWidth = 80  // Target SDK
    }

    private fun createActionPanel(): JPanel {
        val panel = JPanel()

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { refresh() }
        panel.add(refreshButton)

        return panel
    }

    fun setDevice(device: AdbDevice) {
        currentDevice = device
        statusLabel.text = "Loading packages from ${device.name}..."
        refresh()
    }

    fun refresh() {
        val device = currentDevice ?: return

        scope.launch {
            try {
                statusLabel.text = "Loading packages..."
                val packages = commandExecutor.listPackages(device)

                SwingUtilities.invokeLater {
                    packagesTableModel.updatePackages(packages)
                    statusLabel.text = "Found ${packages.size} packages"
                }
            } catch (e: Exception) {
                LOG.error("Failed to load packages", e)
                SwingUtilities.invokeLater {
                    statusLabel.text = "Error loading packages: ${e.message}"
                }
            }
        }
    }

    /**
     * Table model for packages
     */
    private class PackagesTableModel : AbstractTableModel() {
        private val packages = mutableListOf<AppPackage>()
        private val columnNames = arrayOf("Package Name", "Version Name", "Version Code", "Target SDK")

        override fun getRowCount(): Int = packages.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val pkg = packages[rowIndex]
            return when (columnIndex) {
                0 -> pkg.packageName
                1 -> pkg.versionName.ifBlank { "N/A" }
                2 -> if (pkg.versionCode >= 0) pkg.versionCode.toString() else "N/A"
                3 -> if (pkg.targetSdk >= 0) pkg.targetSdk.toString() else "N/A"
                else -> ""
            }
        }

        fun updatePackages(newPackages: List<AppPackage>) {
            packages.clear()
            packages.addAll(newPackages)
            fireTableDataChanged()
        }

        fun getPackageAt(rowIndex: Int): AppPackage = packages[rowIndex]
    }
}
