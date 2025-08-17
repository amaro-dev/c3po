# Android Package Data Available via ADB

## Current Implementation Analysis

### AppPackage Model

```kotlin
data class AppPackage(
    val packageName: String,
    val versionName: String = "",
    val versionCode: Int = -1,
    val targetSdk: Int = -1,
    val signerInfo: SignatureInfo? = null,
    val sleepState: SleepState = SleepState.Unknown,
)
```

### ListPackagesCommand

- Uses `dumpsys package` command
- Parses package information from system dump
- Currently extracts: packageName, versionName, versionCode, targetSdk

## Available ADB Commands for Package Information

### 1. `pm list packages` variations

- `pm list packages`: All packages
- `pm list packages -s`: System packages only
- `pm list packages -3`: Third-party packages only
- `pm list packages -u`: Include uninstalled packages
- `pm list packages -e`: Enabled packages only
- `pm list packages -d`: Disabled packages only

### 2. `dumpsys package` (currently used)

Contains comprehensive package information including:

- Installation flags (system/user)
- Permissions
- Component states
- Debug/release mode
- Installation source

### 3. `ps` or `dumpsys activity processes`

- Shows running processes
- Can map package names to running state

### 4. Package Manager queries

- `pm dump [package]`: Detailed package info
- `pm path [package]`: Installation path (can indicate system vs user)

## Required Model Enhancements

### Enhanced AppPackage Model

```kotlin
data class AppPackage(
    val packageName: String,
    val versionName: String = "",
    val versionCode: Int = -1,
    val targetSdk: Int = -1,
    val signerInfo: SignatureInfo? = null,
    val sleepState: SleepState = SleepState.Unknown,
    // New fields for filtering
    val isSystemApp: Boolean = false,
    val isDebuggable: Boolean = false,
    val isRunning: Boolean = false,
    val isEnabled: Boolean = true,
    val installationSource: String? = null,
    val installPath: String? = null
)
```

## Implementation Strategy

### Option 1: Enhanced dumpsys parsing

Enhance ListPackagesCommand to extract additional fields from dumpsys output:

- Look for installation flags in dumpsys package
- Parse debuggable flag from applicationInfo
- Determine system vs user from installation path

### Option 2: Multiple command approach

Create separate commands for specific information:

- ListSystemPackagesCommand (`pm list packages -s`)
- ListUserPackagesCommand (`pm list packages -3`)
- CheckRunningProcessesCommand (`ps` or `dumpsys activity`)

### Option 3: Hybrid approach

Enhance current command with selective additional queries for complex filters