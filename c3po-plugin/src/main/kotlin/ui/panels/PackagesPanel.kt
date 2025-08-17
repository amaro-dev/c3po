package ui.panels

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import core.model.AdbDevice
import core.model.AppPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import services.PluginCommandExecutor
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.table.AbstractTableModel

/**
 * Panel displaying installed packages from all connected devices.
 * Simplified to work with Android Studio's device management.
 */
class PackagesPanel(
    private val commandExecutor: PluginCommandExecutor,
) {
    companion object {
        private val LOG = Logger.getInstance(PackagesPanel::class.java)
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentDevices: List<AdbDevice> = emptyList()

    private val packagesTableModel = PackagesTableModel()
    private val packagesTable = JBTable(packagesTableModel)
    private val statusLabel = JBLabel("Connect devices via Android Studio to view packages")

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
        columnModel.getColumn(0).preferredWidth = 250 // Package Name
        columnModel.getColumn(1).preferredWidth = 100 // Version Name
        columnModel.getColumn(2).preferredWidth = 80 // Version Code
        columnModel.getColumn(3).preferredWidth = 80 // Target SDK
    }

    private fun createActionPanel(): JPanel {
        val panel = JPanel()

        // Future action buttons can be added here

        return panel
    }

    fun setDevices(devices: List<AdbDevice>) {
        currentDevices = devices
        if (devices.isEmpty()) {
            statusLabel.text = "No devices connected"
            SwingUtilities.invokeLater {
                packagesTableModel.updatePackages(emptyList())
            }
        } else {
            statusLabel.text = "Loading packages..."
            refresh()
        }
    }

    fun refresh() {
        scope.launch {
            try {
                SwingUtilities.invokeLater {
                    statusLabel.text = "Loading packages..."
                }

                // Get current devices
                val devices = commandExecutor.getConnectedDevices()
                currentDevices = devices.take(1) // Take only first device

                if (currentDevices.isEmpty()) {
                    SwingUtilities.invokeLater {
                        statusLabel.text = "No devices connected"
                        packagesTableModel.updatePackages(emptyList())
                    }
                    return@launch
                }

                val allPackages = mutableListOf<PackageWithDevice>()

                // Load packages from first device only
                val device = currentDevices.first()
                try {
                    val packages = commandExecutor.listPackages(device)
                    val packagesWithDevice = packages.map { PackageWithDevice(device, it) }
                    allPackages.addAll(packagesWithDevice)
                } catch (e: Exception) {
                    LOG.error("Failed to load packages from device ${device.name}", e)
                }

                SwingUtilities.invokeLater {
                    packagesTableModel.updatePackages(allPackages)
                    statusLabel.text = "Found ${allPackages.size} packages"
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
     * Data class to hold package with its associated device
     */
    private data class PackageWithDevice(
        val device: AdbDevice,
        val appPackage: AppPackage,
    )

    /**
     * Table model for packages with device information
     */
    private class PackagesTableModel : AbstractTableModel() {
        private val packages = mutableListOf<PackageWithDevice>()
        private val columnNames = arrayOf("Package Name", "Version Name", "Version Code", "Target SDK")

        override fun getRowCount(): Int = packages.size

        override fun getColumnCount(): Int = columnNames.size

        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(
            rowIndex: Int,
            columnIndex: Int,
        ): Any {
            val packageWithDevice = packages[rowIndex]
            val pkg = packageWithDevice.appPackage
            return when (columnIndex) {
                0 -> pkg.packageName
                1 -> pkg.versionName.ifBlank { "N/A" }
                2 -> if (pkg.versionCode >= 0) pkg.versionCode.toString() else "N/A"
                3 -> if (pkg.targetSdk >= 0) pkg.targetSdk.toString() else "N/A"
                else -> ""
            }
        }

        fun updatePackages(newPackages: List<PackageWithDevice>) {
            packages.clear()
            packages.addAll(newPackages)
            fireTableDataChanged()
        }

        fun getPackageAt(rowIndex: Int): PackageWithDevice = packages[rowIndex]
    }
}
