package ui.panels

import com.intellij.ui.components.JBLabel
import models.AdbDevice
import javax.swing.*
import java.awt.FlowLayout

/**
 * Panel for device selection and management.
 * Shows connected devices and allows user to select which device to work with.
 */
class DevicePanel(
    private val onDeviceSelected: (AdbDevice?) -> Unit,
    private val onRefreshRequested: () -> Unit = {}
) {

    private val deviceComboBox = JComboBox<DeviceItem>()
    private val refreshButton = JButton("Refresh")

    fun createPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))

        panel.add(JBLabel("Device:"))

        // Setup device combo box
        deviceComboBox.addActionListener {
            val selectedItem = deviceComboBox.selectedItem as? DeviceItem
            onDeviceSelected(selectedItem?.device)
        }
        panel.add(deviceComboBox)

        // Refresh button
        refreshButton.addActionListener {
            onRefreshRequested()
        }
        panel.add(refreshButton)

        return panel
    }

    fun updateDevices(devices: List<AdbDevice>) {
        SwingUtilities.invokeLater {
            val currentSelection = deviceComboBox.selectedItem as? DeviceItem

            deviceComboBox.removeAllItems()

            if (devices.isEmpty()) {
                deviceComboBox.addItem(DeviceItem(null, "No devices connected"))
                deviceComboBox.isEnabled = false
            } else {
                deviceComboBox.isEnabled = true
                devices.forEach { device ->
                    deviceComboBox.addItem(DeviceItem(device, "${device.name} (${device.id})"))
                }

                // Try to maintain selection
                currentSelection?.device?.let { selectedDevice ->
                    val matchingIndex = devices.indexOfFirst { it.id == selectedDevice.id }
                    if (matchingIndex >= 0) {
                        deviceComboBox.selectedIndex = matchingIndex
                    }
                }
            }
        }
    }

    /**
     * Wrapper class for combo box items
     */
    private data class DeviceItem(
        val device: AdbDevice?,
        val displayText: String
    ) {
        override fun toString(): String = displayText
    }
}
