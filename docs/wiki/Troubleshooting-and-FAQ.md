---
title: "Troubleshooting and FAQ"
layout: default
---

# Troubleshooting and FAQ

Quick solutions for common C3PO issues and answers to frequently asked questions. For detailed setup instructions, see the [Getting Started Guide](Getting-Started-Guide.html).

## Quick Error Reference

### Common ADB Errors

| Error Message          | Quick Fix                                          |
|------------------------|----------------------------------------------------|
| `device not found`     | Check USB connection: `adb devices`                |
| `device offline`       | Restart ADB: `adb kill-server && adb start-server` |
| `unauthorized`         | Check device screen for authorization dialog       |
| `protocol fault`       | Reconnect USB cable and restart ADB server         |
| `more than one device` | Use device selector in C3PO interface              |

### C3PO Connection Issues

| Problem                   | Solution                                                                       |
|---------------------------|--------------------------------------------------------------------------------|
| No devices shown          | Verify ADB path in Settings → Test Connection                                  |
| "ADB not configured"      | See [Getting Started Guide](Getting-Started-Guide.html#configuring-adb-location) |
| Connection keeps dropping | Try different USB cable/port                                                   |
| Plugins show no data      | Check device authorization status                                              |

## System-Level Issues

### ADB Server Problems

#### Multiple ADB Versions Conflict

```bash
# Find all ADB installations
find /usr /opt ~/Library -name "adb" 2>/dev/null

# Use consistent version
export PATH="/opt/homebrew/bin:$PATH"  # Apple Silicon
export PATH="/usr/local/bin:$PATH"     # Intel Mac
```

#### ADB Server Won't Start

```bash
# Kill all ADB processes
adb kill-server
pkill -f adb

# Restart clean
adb start-server
```

### macOS Security Issues

#### Gatekeeper Warnings

```bash
# Remove quarantine attribute
xattr -dr com.apple.quarantine /Applications/C3PO.app

# Or: System Preferences → Security & Privacy → "Open Anyway"
```

#### USB Access Permissions

- System Preferences → Security & Privacy → Privacy → USB
- Add Terminal.app and C3PO.app to allowed applications
- For macOS Ventura+: Allow USB accessories

### Apple Silicon Compatibility

#### Performance Issues on M1/M2/M3 Macs

```bash
# Verify native ARM64 installation
arch
# Should return "arm64" not "i386"

# Install native Java
brew install --cask temurin@17

# Check Java architecture
java --version
```

## Network and Wireless Issues

### Wireless ADB Problems

#### Connection Setup

```bash
# Enable wireless debugging on device
# Settings → Developer Options → Wireless debugging

# Connect via WiFi
adb tcpip 5555
adb connect [device-ip]:5555
```

#### Corporate Network Restrictions

- Use USB connection instead of wireless
- Contact IT for ADB port access (5555-5585)
- Bypass VPN for local device connections

## Performance Issues

### Slow Response Times

**Quick fixes:**

- Close unused C3PO plugins
- Clear device logs: `adb logcat -c`
- Check available storage: `adb shell df -h`
- Restart C3PO application

### Memory Issues

**Symptoms:** Java heap space errors, crashes
**Solutions:**

- Close other ADB-using applications
- Restart C3PO periodically during heavy usage
- Check system memory: `vm_stat`

## Frequently Asked Questions

### Device Compatibility

**Q: Does C3PO work with emulators?**  
A: Yes, works with Android Studio AVD and other ADB-compatible emulators.

**Q: Can I use multiple devices simultaneously?**  
A: Yes, use the device selector dropdown to switch between connected devices.

**Q: Does C3PO work with rooted devices?**  
A: Yes, works with both rooted and non-rooted devices.

### Security and Privacy

**Q: What data does C3PO collect?**  
A: All processing is local on your Mac. No device data is uploaded.

**Q: Is it safe for work devices?**  
A: C3PO uses standard ADB protocols. Check your organization's development tool policies.

**Q: Can C3PO access personal data?**  
A: Only accesses information available through ADB with explicit user authorization.

### Technical Requirements

**Q: Why Java 17 required?**  
A: Modern Java features provide better performance and security.

**Q: Can I run C3PO on Windows/Linux?**  
A: Currently macOS only. Other platforms not officially supported.

**Q: How do I update C3PO?**  
A: Download latest version from GitHub releases. Settings are preserved.

**Q: Does C3PO need internet access?**  
A: No, works completely offline after initial setup.

### Common Usage Questions

**Q: My device keeps disconnecting**  
A: Usually USB power management. Try different cable/port, check for loose connections.

**Q: C3PO performance is slow**  
A: Check device storage, reduce running apps, restart C3PO, close unused plugins.

**Q: Some features don't work**  
A: Feature availability depends on Android version, device manufacturer, and authorization level.

**Q: Can I automate C3PO operations?**  
A: Use the [Automation plugin](Automation-and-Scripting.html) for scripting common tasks.

## Plugin-Specific Issues

### Quick Plugin Troubleshooting

- **Packages not loading:** Check USB debugging authorization
- **Activities won't launch:** Verify activity exists: `adb shell dumpsys package [app] | grep -i activity`
- **Permissions empty:** Device may restrict permission listing
- **Signature analysis fails:** Ensure APK file is not corrupted

**For detailed plugin troubleshooting:** See individual feature guides

- [Package Management Issues](Package-Management-Deep-Dive.html)
- [Activities and Services Issues](Activities-and-Services-Management.html)
- [Permissions and Security Issues](Security-and-Permissions-Analysis.html)

## Debug Information

### Gathering Logs for Support

1. **Enable debug logging:** C3PO → Settings → Enable Debug Logging
2. **Reproduce issue**
3. **Collect logs:** `~/Library/Logs/C3PO/`
4. **ADB logs:** `export ADB_TRACE=all && adb devices`

### System Information Checklist

When reporting issues, include:

- C3PO version
- macOS version (`sw_vers`)
- Java version (`java --version`)
- ADB version (`adb version`)
- Device model and Android version
- Steps to reproduce

## File Dialog Issues on macOS

### File Dialogs Not Appearing

**Symptoms:**

- Clicking "Open Script" or "Select APK" buttons does nothing
- File selection dialogs don't appear
- No error messages shown

**Cause:** macOS security restrictions prevent the app from accessing file system dialogs without proper permissions.

**Solution:**

1. **Grant File Access Permissions:**
    - Open **System Preferences** (or **System Settings** on macOS 13+)
    - Go to **Security & Privacy** → **Privacy**
    - Select **"Files and Folders"** from the left sidebar
    - Find **C3PO** in the list and check the boxes for:
        - ✅ Documents Folder
        - ✅ Downloads Folder
        - ✅ Desktop Folder

2. **Alternative: Full Disk Access** (if above doesn't work):
    - In the same **Privacy** section, select **"Full Disk Access"**
    - Click the **"+"** button and add the C3PO application
    - Or add **Java** if you're running from command line

3. **Check Permission Status:**
    - Open C3PO Settings
    - Look for the **"File Permissions"** section
    - Click **"Check Status"** to see current access levels
    - Follow the displayed instructions if issues are detected

4. **Restart Application:**
    - Close C3PO completely
    - Reopen the application for permissions to take effect

**Technical Details:**

- C3PO uses native macOS file dialogs for security compliance
- macOS Catalina+ requires explicit permission grants for file system access
- Permissions are tied to the specific application bundle or Java executable

### File Dialog Appears But Can't Access Folders

**Problem:** Dialog appears but shows "Permission denied" or can't browse certain folders.

**Solutions:**

1. Grant permissions as described above
2. Try browsing to a different folder first (like Desktop)
3. Use the manual file path input when the dialog offers it

### File Paths with Spaces

**Problem:** Selected file paths with spaces cause errors.

**Solution:** This is handled automatically by the application, but if issues persist:

- Avoid file paths with special characters when possible
- Use the file dialog rather than typing paths manually

## Getting Additional Help

**Still experiencing issues?**

1. **Check existing solutions:**
    - [Getting Started Guide](Getting-Started-Guide.html) - Setup and configuration
    - Feature-specific guides for detailed troubleshooting

2. **Report new issues:**
    - [GitHub Issues](../../../issues)
    - Include system information and debug logs
    - Provide clear steps to reproduce the problem

3. **Community support:**
    - Search existing GitHub discussions
    - Check release notes for known issues