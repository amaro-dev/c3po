<p align="center">
  <img src="icon.iconset/icon_128x128.png" alt="C3PO Logo" width="128" height="128">
</p>

<h1 align="center"> C3PO - Android Device Explorer </h1>

<p align="center">
  <strong>A powerful macOS desktop application for exploring, debugging, and managing Android devices via ADB</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-macOS-blue.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Kotlin-2.0.0-purple.svg" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose-Desktop-green.svg" alt="Compose Desktop">
  <img src="https://img.shields.io/github/v/release/amaro-dev/c3po" alt="Version">
  <img src="https://img.shields.io/badge/Java-17+-red.svg" alt="Java">
<br/>
<br/>
<a href="https://www.paypal.com/donate/?business=ZDZ84AQCFHW3S&no_recurring=0&item_name=C3po+donation&currency_code=BRL">
      <img alt="Buy me a beer" src="https://img.shields.io/badge/🍺-Buy%20me%20a%20beer-brightgreen">
    </a>
</p>

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Setup](#setup)
- [Features](#features)
- [Usage](#usage)
- [Troubleshooting](#troubleshooting)
- [Privacy & Security](#privacy--security)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)

## Overview

**C3PO** (named after the helpful protocol droid from Star Wars) is a comprehensive macOS desktop application built with
Kotlin and Jetpack Compose Desktop. It serves as your faithful companion for exploring, debugging, and managing Android
devices connected via ADB (Android Debug Bridge).

The application provides developers with an intuitive graphical interface to interact with Android devices, offering
deep insights into app internals, system components, and device management through a powerful plugin-based architecture.

## Installation

### Option 1: Download Pre-built Release (Recommended)

1. **Download the latest release:**
    - Visit the [Releases page](https://github.com/amaro-dev/c3po/releases)
    - Download the latest `c3po-X.X.X-macos.dmg` file

2. **Install the application:**
   ```bash
   # Open the downloaded DMG file
   open c3po-*.dmg
   
   # Drag C3PO.app to Applications folder
   # Or double-click the DMG and follow the installation prompts
   ```

3. **First launch:**
    - Open Applications folder and launch C3PO
    - If you see a security warning, go to System Preferences > Security & Privacy > General
    - Click "Open Anyway" next to the C3PO warning

### Option 2: Build from Source

**Prerequisites:**

- Java 17 or later
- Git

```bash
# Clone the repository
git clone https://github.com/amaro-dev/c3po.git
cd c3po

# Build the application
./gradlew build

# Run directly
./gradlew run

# Create macOS distribution
./gradlew packageDmg
# DMG file will be created in: c3po-desktop/build/compose/binaries/main/dmg/
```

### System Requirements

- **macOS:** 10.14 Mojave or later
- **Architecture:** Apple Silicon (M1/M2)
- **Memory:** Minimum 512MB RAM
- **Storage:** 150MB available space

## Setup

### 1. Install Android Platform Tools

C3PO requires ADB (Android Debug Bridge) to communicate with Android devices.

**Using Homebrew (Recommended):**

```bash
# Install Android Platform Tools
brew install android-platform-tools

# Verify installation
adb version
```

**Manual Installation:**

1. Download Android SDK Platform Tools from [Google](https://developer.android.com/studio/releases/platform-tools)
2. Extract to a folder (e.g., `/usr/local/bin/platform-tools`)
3. Add to PATH:
   ```bash
   # For Apple Silicon Macs
   export PATH="/opt/homebrew/bin:$PATH"
   
   # Add to your ~/.zshrc or ~/.bash_profile for persistence
   echo 'export PATH="/opt/homebrew/bin:$PATH"' >> ~/.zshrc
   ```

### 2. Configure ADB Location in C3PO

When you launch C3PO for the first time, the application will automatically open the Settings dialog to help you
configure the ADB location.

*C3PO will show the ADB configuration dialog on first launch*

**You have three options to set up ADB:**

#### Option 1: Manual Path Entry

- **Copy and paste** the ADB path directly into the text field
- Common locations:
  ```bash
  # Homebrew (Apple Silicon)
  /opt/homebrew/bin/adb
  
  # Manual installation
  /usr/local/platform-tools/adb
  ```

#### Option 2: Browse for ADB

- Click the **📁 Browse** button to open a file picker
- Navigate to your ADB installation directory
- Select the `adb` executable file

#### Option 3: Automatic Search

- Click the **🔍 Search** button to let C3PO automatically find ADB
- The app will perform the `which adb`command to get the path
- This is the **recommended option** for most users

**Verification:**
Once configured, C3PO will:

- Test the ADB connection
- Proceed to the main application interface

**Reconfiguring ADB:**

- Access ADB settings anytime via: **C3PO Menu > Preferences > ADB Settings**
- Change ADB location if you update or reinstall Android Platform Tools

### 3. Configure Android Device

1. **Enable Developer Options:**
    - Go to Settings > About Phone
    - Tap "Build Number" 7 times
    - Developer options will appear in Settings

2. **Enable USB Debugging:**
    - Go to Settings > Developer Options
    - Enable "USB Debugging"
    - Enable "Wireless ADB debugging" (optional, for wireless connections)

3. **Connect Device:**
   ```bash
   # Connect via USB and verify
   adb devices
   
   # You should see your device listed
   # If "unauthorized", check your device for authorization prompt
   ```

### 4. Trust Computer

When connecting for the first time, your Android device will show a popup:

- **"Allow USB debugging?"**
- Check "Always allow from this computer"
- Tap "OK"

Verify connection:

```bash
adb devices
# Output should show: [DEVICE_ID]    device
```

## Features

C3PO offers comprehensive Android device management through a powerful plugin-based architecture:

### 📱 Core Device Management

- **Multi-device Support:** Connect and manage multiple Android devices simultaneously
- **Status:** Gather device connection status and health
- **Device Information:** View detailed hardware, software, and system information
- **ADB Integration:** Seamless Android Debug Bridge connectivity with automatic port management

### 🔌 Plugin-Based Architecture

#### Available Plugins:

- **📦 Packages Plugin:** Complete package management
    - List all installed applications
    - View package details (version, signatures, state)
    - Uninstall applications
    - Start and stop applications
    - Clear application data
    - Monitor app sleep states

- **📋 Activities Plugin:** Activity list and instrumentation
    - List available activities
    - Launch specific activities
    - Launch activity in Debug Mode

- **⚙️ Services Plugin:** System and app service control
    - List running services by package
    - Start/stop system and application services

- **🔐 Permissions Plugin:** Security analysis
    - View all declared and available permissions
    - Identify permission flags

- **🏷️ Attributes Plugin:** Device attributes
    - Explore device attributes and properties
    - Copy keys and values

- **🔑 Signature Plugin:** Application signing analysis
    - Extract and verify APK signatures
    - Certificate chain analysis
    - Signing compliance reports
    - Security validation

- **🤖 Automation Plugin:** Scripting and automation
    - Custom script execution
    - Automated testing workflows
    - Bulk operations on devices

## Detailed Feature Guides

For comprehensive walkthroughs with screenshots and examples:

- 🚀 **[Getting Started Guide](https://amaro-dev.github.io/c3po/wiki/Getting-Started-Guide.html)** - First-time setup walkthrough
- 📦 **[Package Management Deep Dive](https://amaro-dev.github.io/c3po/wiki/Package-Management-Deep-Dive.html)** - Complete app management guide
- 📋 **[Activities & Services Management](https://amaro-dev.github.io/c3po/wiki/Activities-and-Services-Management.html)** - Component lifecycle control
- 🔐 **[Security & Permissions Analysis](https://amaro-dev.github.io/c3po/wiki/Security-and-Permissions-Analysis.html)** - Security auditing tools
- 📊 **[Device Information & Monitoring](https://amaro-dev.github.io/c3po/wiki/Device-Information-and-Monitoring.html)** - Hardware specs and performance
- 🤖 **[Automation & Scripting](https://amaro-dev.github.io/c3po/wiki/Automation-and-Scripting.html)** - Custom workflows and batch operations
- ❓ **[Troubleshooting & FAQ](https://amaro-dev.github.io/c3po/wiki/Troubleshooting-and-FAQ.html)** - Common issues and solutions

## Troubleshooting

### Device Not Detected

**Check ADB connection:**

```bash
adb devices
```

**If no devices listed:**

```bash
# Restart ADB server
adb kill-server
adb start-server

# Check USB connection and try different cable/port
```

**If device shows "unauthorized":**

1. Check device screen for authorization dialog
2. Enable "Always allow from this computer"
3. Tap "OK"

### C3PO Cannot Connect to Device

**Verify ADB installation:**

```bash
which adb
adb version
```

**Check PATH configuration:**

```bash
echo $PATH
# Should include Android platform-tools directory
```

**For Apple Silicon Macs:**

```bash
# Add to ~/.zshrc or ~/.bash_profile
export PATH="/opt/homebrew/bin:$PATH"
source ~/.zshrc
```


### Connection Timeout Issues

1. **Increase ADB timeout:**
   ```bash
   # Set longer timeout (default is 5000ms)
   adb connect [device-ip]:5555
   ```

2. **Check device storage:** Low storage can cause connection issues
3. **Restart device:** Sometimes a simple reboot resolves connectivity issues
4. **Try different USB port/cable:** Hardware issues can cause intermittent connections

### App stays in processing state

**Wrong adb path:**

- Open the settings and check if your informed ADB is correct

**Permissions:**

- Open the settings, activate all log levels and save
- Restart the application and, once again, open the settings dialog
- Click on the "Open logs folder" button
- Open the most recent log file in your text editor of preference
- You can find useful info about what is preventing the app to complete the operation

## Privacy & Security

### Data Collection

C3PO prioritizes user privacy and operates with these principles:

- **Local Operation:** All device data processing happens locally on your Mac
- **No Data Upload:** Device information never leaves your machine
- **No Telemetry:** No usage analytics or tracking (except optional crash reporting)
- **Minimal Permissions:** Only requests necessary ADB permissions

## License

This project is licensed under
the [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)](https://creativecommons.org/licenses/by-nc-sa/4.0/).

You are free to use, modify, and share this code **for non-commercial purposes only**, provided that proper credit is
given to the author and derivative works are distributed under the same license.

**Commercial use is strictly prohibited without prior permission.**

## Support

### Getting Help

- **GitHub Issues:** [Report bugs or request features](https://github.com/amaro-dev/c3po/issues)
- **GitHub Discussions:** [Ask questions and get support](https://github.com/amaro-dev/c3po/discussions)
- **Documentation:** [Comprehensive documentation](https://amaro-dev.github.io/c3po/)

### Buy Me a Beer 🍺

If C3PO has been helpful for your Android development workflow, consider supporting the project:

[![Buy me a beer](https://img.shields.io/badge/🍺-Buy%20me%20a%20beer-brightgreen)](https://www.paypal.com/donate/?business=ZDZ84AQCFHW3S&no_recurring=0&item_name=C3po+donation&currency_code=BRL)

