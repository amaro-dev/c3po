# Main Window (Framework) – Screen Documentation

## 1. UI Elements

### Sidebar (Navigation)
- **Description:** Vertical bar on the left with a list of plugin buttons (icons + labels).
- **Elements:** Each button represents a plugin (Packages, Permissions, Activities, Services, Device Attributes, Signature, Automation).
- **States:** 
  - Default: Icon and label.
  - Hover: Background tinted with primary light color.
  - Selected: Highlighted with accent color.
- **Actions:** Clicking a button navigates to the corresponding plugin screen.

### Top Bar
- **Device Selector:** Drop-down listing connected devices.
  - **Actions:** Selecting a device triggers device change.
- **Refresh Devices Button:** Icon button to refresh the device list.
  - **Actions:** Clicking refreshes the list of connected devices.
- **Companion Status Indicator/Button:** Shows current status (e.g., installed, missing) and opens the companion installation dialog.
  - **Actions:** Clicking opens the companion dialog.
- **Global Feedback Area:** Displays running status (spinner) or error overlays.

### Content Area
- **Description:** Main area that displays the currently selected plugin’s UI.
- **States:** 
  - Shows plugin content if a device is selected and settings are configured.
  - Shows placeholder if no device is selected.
  - Disabled/blurred if a blocking overlay (settings, error, companion) is active.

### Overlays (Global Dialogs)
- **Settings Overlay:** Modal dialog for configuring the ADB path.
- **Companion Dialog:** Modal for installing the companion app.
- **Feedback Overlay:** Shows running status (spinner + description) or error messages with a dismiss button.

---

## 2. How Elements Change Over Time

- **Sidebar:** The selected plugin is highlighted as the user navigates.
- **Device Selector:** Updates as devices are connected/disconnected.
- **Global Feedback Area:** 
  - Shows spinner when commands are running.
  - Shows error overlay when an error occurs.
- **Content Area:** Changes to display the selected plugin’s UI or placeholder if no device is selected.
- **Overlays:** Appear/disappear based on app state (e.g., missing settings, errors, companion install required).

---

## 3. Possible User Actions

- **Select Plugin:** Click a sidebar button to switch plugin screens.
- **Select Device:** Choose a device from the top bar drop-down.
- **Refresh Devices:** Click the refresh button to update the device list.
- **Open Companion Dialog:** Click the companion status indicator.
- **Dismiss Error:** Click the dismiss button on the error overlay.
- **Configure Settings:** Interact with the settings overlay to set the ADB path.
- **Install/Skip Companion:** Use the companion dialog to install or skip installation.

---

## 4. Layout of the Screen

- **Left:** Sidebar (vertical, fixed width).
- **Top (above content):** Top bar (horizontal, full width of content area).
- **Center:** Main content area (fills remaining space).
- **Overlays:** Modal dialogs centered over the content area, with a scrim to block interaction with the underlying UI.

---

## 5. How User Actions Change the Screen

- **Selecting a Plugin:** Updates the content area to show the selected plugin’s UI.
- **Selecting a Device:** Updates the device context for all plugin screens.
- **Refreshing Devices:** Updates the device selector with the latest connected devices.
- **Opening Overlays:** Blocks interaction with the main content until the overlay is dismissed or completed.
- **Dismissing Error Overlay:** Removes the error message and returns to normal UI state.
- **Configuring Settings:** Once a valid ADB path is saved, the settings overlay disappears and the app becomes usable.
- **Installing/Skipping Companion:** Updates the companion status and may enable further features.

---

## 6. Dialogs That Open in This Screen

### Settings Overlay
- **When:** Shown if the ADB path is not configured.
- **Options:** Input field for ADB path, Save button, Cancel/Close.
- **Interaction:** Saving a valid path enables the rest of the app; cancel/close keeps the overlay open if required.

### Companion Dialog
- **When:** Shown if the companion app is required but not installed.
- **Options:** Install button, Skip button, Cancel/Close.
- **Interaction:** Installing triggers the install process; skipping/canceling may limit functionality.

### Feedback Overlay
- **When:** Shown during long-running commands or on error.
- **Options:** Spinner and description (for running), error message and dismiss button (for errors).
- **Interaction:** Dismissing an error overlay resets the error state.

---

Let me know if you want this in a Markdown file or want to proceed with the next screen!
