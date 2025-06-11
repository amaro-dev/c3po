# C3PO Utilities & Features Specification

This document provides a comprehensive technical specification of all utilities, features, and capabilities available in C3PO. This serves as a complete reference for implementing enhancements, fixes, and new features.

## Table of Contents

- [Core System Architecture](#core-system-architecture)
- [Device Management Utilities](#device-management-utilities)
- [ADB Command System](#adb-command-system)
- [Plugin System](#plugin-system)
- [Socket Communication](#socket-communication)
- [Companion Service Management](#companion-service-management)
- [User Interface Components](#user-interface-components)
- [Settings & Configuration](#settings--configuration)
- [Extension Points](#extension-points)

## Core System Architecture

### State Management (Redux Pattern)

**Location**: `src/main/kotlin/core/`

#### Core Components
- **AppState**: Central application state container
- **Actions**: All possible user actions and system events
- **AppReducer**: State transformation logic
- **AppStateManager**: State orchestration and middleware coordination
- **Middleware Pipeline**: Action processing chain

#### Key State Objects
```kotlin
data class AppState(
    val devices: List<AdbDevice>,
    val currentDevice: AdbDevice?,
    val companionState: CompanionState,
    val commandStatus: CommandStatus,
    val currentPlugin: String?,
    val pluginStates: Map<String, WindowResult<*>>,
    val settings: SettingsState
)
```

#### Available Actions
- Device management: `SelectDevice`, `RefreshDevices`
- Plugin control: `SelectPlugin`, `ChangeFilter`
- Command execution: `ExecuteCommand`, `DeliverSocketResponse`
- Settings: `LoadSettings`, `SaveSettings`
- Clipboard: `CopyToClipboard`

## Device Management Utilities

### Device Discovery & Connection

**Components**: `DeviceMiddleware`, `CompanionMiddleware`

#### Capabilities
- **Multi-device Support**: Detect and manage multiple connected Android devices
- **Connection Health Monitoring**: Real-time status tracking
- **Automatic Retry Logic**: Intelligent reconnection attempts
- **Port Forwarding Management**: ADB port configuration and forwarding

#### Device Connection Flow
1. **Detection**: List available ADB devices
2. **Selection**: User selects target device
3. **Companion Check**: Verify companion service status
4. **Installation**: Auto-install companion service if needed
5. **Service Management**: Start/stop companion service
6. **Port Setup**: Configure ADB port forwarding (port 9500)
7. **Socket Connection**: Establish communication channel

#### Device Information Retrieval
- Device model and manufacturer
- Android version and API level
- Screen resolution and density
- Available storage and memory
- Network configuration

## ADB Command System

**Location**: `src/main/kotlin/commands/`

### Command Infrastructure

#### Core Components
- **CommandExecutor**: Command execution coordinator
- **CommandRunner**: Low-level ADB command execution
- **CommandBuilder**: Dynamic command construction
- **AdbCommand Interface**: Standard command contract

### Available Commands

#### Package Management Commands
- **ListPackagesCommand**: Retrieve all installed packages with metadata
  - Package name, version name, version code, target SDK
  - Parsing from `dumpsys package` output
  - Automatic sorting by package name

- **UninstallAppCommand**: Remove applications from device
- **StopAppCommand**: Force-stop running applications
- **ClearDataCommand**: Clear application data and cache
- **InstallApkCommand**: Install APK files to device
- **CheckAppInstalledCommand**: Verify package installation status

#### Activity Management Commands
- **ListActivitiesCommand**: Enumerate available activities per package
- **StartActivityCommand**: Launch specific activities with intent data
- **ListPendingActivityIntentsCommand**: Retrieve pending intent information

#### Service Management Commands
- **ListServicesCommand**: List running and available services
- **StartServiceCommand**: Start Android services
- **StopServiceCommand**: Stop running services
- **CheckServiceRunningCommand**: Verify service status

#### Permission Analysis Commands
- **ListDeclaredPermissions**: Extract all declared permissions
  - Dangerous, normal, signature permissions
  - Permission groups and protection levels
  - Grant status and usage analysis

#### Device Utility Commands
- **ListDevicesCommand**: Enumerate connected ADB devices
- **DeviceInfoCommand**: Retrieve device specifications
- **ForwardPortCommand**: Configure ADB port forwarding
- **CheckPortForwardCommand**: Verify port forwarding status

### Command Execution Pipeline

```kotlin
interface AdbCommand<T> {
    val command: String
    fun parse(result: String): T
}
```

**Execution Flow**:
1. Command construction with device context
2. ADB process execution
3. Output parsing and transformation
4. Result delivery through state system
5. Error handling and retry logic

## Plugin System

**Location**: `src/main/kotlin/plugins/`

### Plugin Architecture

#### Base Plugin Interface
```kotlin
interface Plugin<in T> {
    val id: String
    val name: String
    val middleware: IMiddleware<AppState>
    fun isResponsibleFor(action: IAction): Boolean
    @Composable fun render(state: Map<String, WindowResult<*>>, onAction: OnAction)
    @Composable fun present(result: WindowResult<T>, onAction: OnAction)
}
```

### Available Plugins

#### 1. Packages Plugin (`PACKAGES`)
**Location**: `src/main/kotlin/plugins/packages/`

**Capabilities**:
- **Package Listing**: Display all installed applications
- **Version Information**: Show version names, codes, and target SDK
- **Lifecycle Management**: Stop, uninstall, clear data
- **Signature Analysis**: Extract and display signing certificates
- **Sleep State Monitoring**: Check app power optimization status
- **Search & Filter**: Real-time package filtering

**Actions**:
- `List`: Retrieve package information
- `Stop(packageInfo)`: Force-stop application
- `Uninstall(packageInfo)`: Remove package
- `ExtractKey(packageInfo)`: Get signing certificate
- `ClearData(packageInfo)`: Clear app data
- `CheckAsleep(packageInfo)`: Verify sleep state

**UI Components**:
- Package list with actionable rows
- Signature information cards
- Version and metadata display
- Sleep state indicators

#### 2. Activities Plugin (`ACTIVITIES`)
**Location**: `src/main/kotlin/plugins/activities/`

**Capabilities**:
- **Activity Discovery**: List all launchable activities
- **Intent Launch**: Start activities with custom intents
- **Package Grouping**: Organize activities by parent package
- **Search & Filter**: Find specific activities

**Actions**:
- `LIST`: Enumerate activities
- `Launch(activityInfo)`: Start activity

#### 3. Services Plugin (`SERVICES`)
**Location**: `src/main/kotlin/plugins/services/`

**Capabilities**:
- **Service Enumeration**: List system and app services
- **Service Control**: Start and stop services
- **Status Monitoring**: Check service running status
- **Package Grouping**: Organize by service owner

**Actions**:
- `LIST`: Retrieve service information
- `Launch(activityInfo)`: Start service

#### 4. Permissions Plugin (`PERMISSIONS`)
**Location**: `src/main/kotlin/plugins/permissions/`

**Capabilities**:
- **Permission Analysis**: Parse declared permissions from all apps
- **Protection Level Display**: Show permission danger levels
- **Grant Status**: Display permission grant state
- **Security Assessment**: Highlight dangerous permissions
- **Package Grouping**: Group permissions by declaring app

**Features**:
- Permission stamps (granted/denied indicators)
- Protection level color coding
- Dangerous permission highlighting
- Search and filtering by permission name

#### 5. Device Attributes Plugin (`ATTRS`)
**Location**: `src/main/kotlin/plugins/attrs/`

**Capabilities**:
- **Device Information**: Hardware and software specifications
- **System Properties**: Android build information
- **Hardware Features**: Available device capabilities
- **Display Information**: Screen properties and DPI

#### 6. Signature Analysis Plugin (`SIGNATURE`)
**Location**: `src/main/kotlin/plugins/signature/`

**Capabilities**:
- **Certificate Extraction**: Extract signing certificates
- **Signature Verification**: Validate app signatures
- **Security Compliance**: Check signing best practices
- **Certificate Chain Analysis**: Examine certificate hierarchy

**Components**:
- Compliance checking
- Security report generation
- Certificate information display
- Signature validation status

#### 7. Pending Intents Plugin (`PENDING_INTENTS`)
**Location**: `src/main/kotlin/plugins/intents/pending/`

**Capabilities**:
- **Intent Discovery**: Find pending intents in system
- **Intent Analysis**: Parse intent data and extras
- **Activity Context**: Show associated activities
- **Intent Actions**: Display intent action types

### Plugin Extension System

**Registration**: Plugins are registered through dependency injection in `FacadeModule`

**Middleware**: Each plugin provides middleware for action handling and state management

**UI Integration**: Plugins provide Compose UI components that integrate with the main interface

## Socket Communication

**Location**: `src/main/kotlin/socket/`

### Socket Client Architecture

**Purpose**: Bi-directional communication with Android companion service

**Port**: 9500 (forwarded via ADB)

**Protocol**: Custom message-based protocol

### Communication Flow
1. **Connection Establishment**: Connect to forwarded port
2. **Message Exchange**: Send commands and receive responses
3. **Response Parsing**: Process structured responses
4. **State Updates**: Deliver results to application state

### Socket Commands
- Device information retrieval
- Enhanced package analysis
- Real-time system monitoring
- Advanced permission analysis

## Companion Service Management

**Location**: `src/main/kotlin/facade/CompanionCommander.kt`

### Service Lifecycle Management

**Companion Package**: `dev.amaro.c3po.companion`
**Service Name**: `CompanionService`
**APK Location**: `src/main/resources/R2D2.apk`

#### Management Operations
- **Installation Detection**: Check if companion app is installed
- **Automatic Installation**: Install companion APK when needed
- **Service Control**: Start and stop companion service
- **Port Configuration**: Set up ADB port forwarding
- **Health Monitoring**: Verify service status and connectivity

#### Connection States
```kotlin
sealed class CompanionState {
    object Unknown : CompanionState()
    object NotInstalled : CompanionState()
    object Installing : CompanionState()
    object Installed : CompanionState()
    object Starting : CompanionState()
    object Running : CompanionState()
    object Connected : CompanionState()
    object Error : CompanionState()
}
```

## User Interface Components

**Location**: `src/main/kotlin/ui/`

### Core UI Components

#### Layout Components
- **MainScreen**: Primary application layout
- **ContentBox**: Plugin content container with search
- **ActionableRow**: Interactive list items with actions
- **HeaderRow**: Section headers for grouped content

#### Interactive Components
- **DeviceSelector**: Dropdown for device selection
- **PluginSelector**: Plugin navigation interface
- **SearchBox**: Real-time filtering input
- **StatusIndicators**: Connection and service status displays

#### Specialized Components
- **SignatureCard**: Certificate information display
- **PermissionRow**: Permission details with status
- **ComplianceBox**: Security compliance indicators
- **DeviceAttrRow**: Device attribute display

### UI Theming & Styling

**Theme System**: Material Design with custom color scheme
**Responsive Design**: Adaptive layouts for different window sizes
**Accessibility**: Screen reader support and keyboard navigation

### UI State Management
- Real-time updates from application state
- Reactive UI updates via Compose state
- Search and filter state management
- Loading and error state handling

## Settings & Configuration

**Location**: `src/main/kotlin/Settings.kt`, `src/main/kotlin/facade/SettingsRepository.kt`

### Configuration Options
- **ADB Path**: Custom ADB executable location
- **Window Preferences**: Size and position settings
- **Plugin Defaults**: Default plugin selection
- **Debug Settings**: Development and troubleshooting options

### Settings Persistence
- **Storage**: Local file-based configuration
- **Format**: JSON-based settings file
- **Auto-save**: Automatic settings persistence
- **Migration**: Settings version management

## Extension Points

### Adding New Plugins

#### Plugin Development Template
```kotlin
class CustomPlugin(executor: CommandExecutor) : Plugin<CustomDataType> {
    sealed interface Actions : IAction {
        // Define plugin-specific actions
    }

    override val id: String = "CUSTOM_ID"
    override val name: String = "Custom Plugin"
    override val middleware: IMiddleware<AppState> = CustomMiddleware(id, executor)

    override fun isResponsibleFor(action: IAction): Boolean = action is Actions

    @Composable
    override fun present(result: WindowResult<CustomDataType>, onAction: OnAction) {
        // Implement UI presentation
    }
}
```

#### Integration Steps
1. **Create Plugin Directory**: `src/main/kotlin/plugins/custom/`
2. **Implement Plugin Interface**: Extend base `Plugin<T>` interface
3. **Create Middleware**: Handle plugin-specific actions and state
4. **Add ADB Commands**: Create necessary command implementations
5. **Register Plugin**: Add to dependency injection configuration
6. **Write Tests**: Comprehensive unit and integration tests

### Adding New ADB Commands

#### Command Development Template
```kotlin
class CustomCommand : AdbCommand<ResultType> {
    override val command: String = "shell custom-command"

    override fun parse(result: String): ResultType {
        // Parse ADB output and return structured data
    }
}
```

### UI Component Extensions

#### Custom UI Components
- Extend base UI components for specialized displays
- Implement consistent styling and theming
- Add accessibility features
- Support responsive design patterns

### Socket Protocol Extensions

#### Message Format
- Define structured message formats
- Implement parsing and serialization
- Add error handling and validation
- Support versioned protocol evolution

## Performance Considerations

### Optimization Areas
- **Lazy Loading**: Load plugin data on demand
- **Caching**: Cache frequently accessed data
- **Background Processing**: Async command execution
- **Memory Management**: Efficient object lifecycle
- **UI Responsiveness**: Non-blocking UI operations

### Monitoring & Metrics

**Telemetry**: Sentry integration for error tracking
**Performance Metrics**: Command execution timing
**Usage Analytics**: Feature usage patterns
**Crash Reporting**: Automatic error reporting

## Security Features

### Security Considerations
- **ADB Security**: Secure ADB connection handling
- **Permission Analysis**: Highlight security risks
- **Certificate Validation**: Verify app signatures
- **Data Protection**: Secure handling of sensitive data

### Compliance Features
- **Signature Verification**: Validate app authenticity
- **Permission Auditing**: Identify permission misuse
- **Security Reporting**: Generate security assessments
- **Best Practice Checking**: Compliance verification

## Testing Infrastructure

### Test Categories
- **Unit Tests**: Individual component testing
- **Integration Tests**: Plugin and middleware testing
- **UI Tests**: Compose UI component testing
- **Command Tests**: ADB command parsing and execution

### Test Coverage
- **Exclusions**: UI components, socket clients, DI modules
- **Minimum Coverage**: Enforced via Kover
- **Mock Support**: MockK for dependency mocking
- **Coroutines Testing**: Async operation testing

## Future Enhancement Areas

### Potential Improvements
1. **Real-time Monitoring**: Live system state updates
2. **Advanced Analytics**: Deep app behavior analysis
3. **Automation Scripts**: Scriptable device operations
4. **Multi-device Coordination**: Synchronized multi-device testing
5. **Cloud Integration**: Remote device management
6. **Plugin Marketplace**: Community plugin ecosystem
7. **Custom Dashboards**: Configurable monitoring views
8. **Export Capabilities**: Report generation and export
9. **Integration APIs**: Third-party tool integration
10. **Enhanced Security**: Advanced threat detection

### Architecture Evolution
- **Microservices**: Split into focused services
- **Cloud Backend**: Centralized device management
- **Web Interface**: Browser-based access
- **Mobile Companion**: Android/iOS companion apps
- **API Gateway**: Standardized API access

---

*This document serves as the comprehensive technical specification for C3PO development. All enhancements, fixes, and new features should reference this document for consistency and completeness.*
