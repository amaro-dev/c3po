# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

C3PO is a desktop Android debugging and exploration tool built with Kotlin and Jetpack Compose Desktop. It connects to Android devices via ADB and provides a plugin-based architecture for exploring apps, activities, services, permissions, and more.

## Setup

### Java SDK Configuration

This project requires Java 17+. To avoid SDK detection issues:

1. **Check for local configuration**: Look in `local-config.properties` for the Java path
2. **Find available Java versions**: Run `/usr/libexec/java_home -V` on macOS
3. **Set the Java path**: Create/update `local-config.properties` with:
   ```
   java.home=/path/to/java/17+/home
   ```
4. **The file is gitignored** to keep machine-specific settings local

## Common Commands

### Building and Running
- **Build**: `./gradlew build`
- **Run**: `./gradlew run`
- **Test**: `./gradlew test`
- **Test with coverage**: `./gradlew koverHtmlReport` (report: `build/reports/kover/html/index.html`)
- **Package macOS DMG**: `./gradlew packageDmg`
- **Clean build**: `./gradlew clean build`

### Module-Specific Commands
- **Build core only**: `./gradlew :c3po-core:build`
- **Build desktop only**: `./gradlew :c3po-desktop:build`
- **Build plugin only**: `./gradlew :c3po-plugin:build`

### Testing Single Components
- **Run specific test**: `./gradlew test --tests "ClassName"`
- **Run tests in package**: `./gradlew test --tests "package.*"`
- **Run module tests**: `./gradlew :c3po-desktop:test`

### Changing a GitHub Project task status

- Locate the GitHub Project that the issue is part of (via MCP or GitHub API).
- Find the **project field or column** related to issue status (e.g., `Status`, `Workflow`, or similar).
- Move the associated card to the `In Progress` column (or set the `Status` field value to `In Progress`), using the
  proper mutation via GitHub GraphQL API or MCP.
- Do **not** update the issue body or leave a comment to indicate the status — you must update the field or column in
  the project itself.
- These are the valid statuses: `Todo`, `In Progress`, `Waiting Approval`, `Done`
- Validate that the issue was correctly moved to the `In Progress` status in the GitHub Project. If the change did not
  occur, stop and show an error message explaining the failure.

You should use this sample mutation to perform the status change:

```
gh api graphql -f query='mutation { updateProjectV2ItemFieldValue(input: { projectId: "P123", itemId: "I456", fieldId: "F789", value: { singleSelectOptionId: "S000" } }) { projectV2Item { id } } }'
```

## Architecture Overview

### Core Components

**Redux-Style State Management**: The app uses a centralized state management pattern with:
- `AppState`: Single source of truth containing device list, current device, plugin state, etc.
- `Action`: Sealed classes defining all possible state changes
- `AppStateManager`: Handles action dispatching and state updates
- `Middleware`: Intercepts actions for side effects (ADB commands, etc.)

**Plugin System**: Extensible architecture where each feature is a plugin:
- `Plugin<T>` interface: Defines plugin contract with `id`, `name`, `icon`, `middleware`, and Compose UI
- Each plugin has its own middleware for handling plugin-specific actions
- Plugins are registered via dependency injection in `di/AppModule.kt`

**Multi-Module Structure**:
- `c3po-core`: Core business logic, commands, models (shared between desktop and Android Studio plugin)
- `c3po-desktop`: Desktop UI implementation with Compose Desktop
- `c3po-plugin`: Android Studio plugin implementation

### Key Architectural Patterns

**Command Pattern**: ADB operations are encapsulated in command classes:
- Base: `AdbCommand` and `EnhancedAdbCommand`
- Examples: `ListPackagesCommand`, `StartActivityCommand`, `ListActivitiesCommand`, `ExtractApkCommand`
- Execution: `CommandExecutor` with strategy pattern for different environments

**APK Signature Extraction**: Integrated system for analyzing Android app signatures:

- `ExtractApkCommand`: Downloads APK files from device to temporary folders
- `ApkSignatureExtractor`: Coordinates APK extraction and signature analysis with cleanup
- `SignatureExtractor`: Uses Android SDK apksigner tool for certificate fingerprint analysis
- Auto-detects Android SDK paths and handles both file and directory-based APK locations
- Automatic temporary file cleanup after analysis

**Dependency Injection**: Uses Koin for DI:
- `di/AppModule.kt`: Main DI configuration
- Named dependencies for plugin lists and other components
- `PluginMiddleware`: Base class for plugin middleware with common ADB execution patterns

## Material Design System

**Theme**: Custom Android Green theme in `ui/AndroidGreenTheme.kt`
- Uses Material3 `ColorScheme` with custom Android green colors
- Light/dark theme support
- Consistent color definitions: `primary = #218c4a`, `surface = #FFFFFF`, etc.

**Custom Components**: 
- `CustomTextField`: 36dp height text field with focus states
- `CustomActionButton`: 28dp buttons with hover/press interactions
- Always use `MaterialTheme.colorScheme` colors, never hardcoded colors

## Plugin Development

### Creating a New Plugin

1. **Create plugin package**: `plugins/yourplugin/`
2. **Implement Plugin interface**:
   ```kotlin
   class YourPlugin : Plugin<YourDataType> {
       override val id = "YOUR_PLUGIN_ID"
       override val name = "Your Plugin"
       override val icon = Icons.Filled.YourIcon
       override val middleware = YourPluginMiddleware()
       
       override fun isResponsibleFor(action: IAction) = action is YourActions
       
       @Composable
       override fun present(result: WindowResult<YourDataType>, onAction: OnAction) {
           // Your Compose UI here
       }
   }
   ```

3. **Create middleware**: Handle actions and execute commands
4. **Register in DI**: Add to plugin list in `di/AppModule.kt`
5. **Define actions**: Sealed interface extending `IAction`

### Existing Plugins
- **Device** (`plugins/device/`): Basic device connection and status
- **Activities** (`plugins/activities/`): Launch activities, view activity lists
- **Packages** (`plugins/packages/`): Manage installed packages, signatures, lifecycle
- **Device Attrs** (`plugins/attrs/`): Device information display
- **Services** (`plugins/services/`): Start/stop services
- **Permissions** (`plugins/permissions/`): View app permissions
- **Signature** (`plugins/signature/`): Analyze application signing and certificates  
- **Automation** (`plugins/automation/`): Script execution and automation workflows

### Plugin Registration Process
Plugins are registered in `di/AppModule.kt` in the `PLUGIN_LIST_DEPENDENCY` factory. Order matters for sidebar display.

## UI Guidelines

**Layout Structure**: New layout (`ui/NewLayout.kt`) with sidebar + main content:
- Sidebar: Plugin selection with Android green theme
- TopBar: Device selector
- ContentArea: Plugin-specific content rendering

**Component Patterns**:
- Use `Card` components with `RoundedCornerShape(12.dp)` for containers
- Package headers: Green background with rounded corners
- Search bars: Custom text fields with filter checkboxes
- Action buttons: Small (28dp) with Material icons and interaction states

**State Management in UI**:
- Listen to `app.listen().collectAsState()` for reactive updates
- Actions dispatched via `onAction: OnAction` parameter
- Local state with `remember { mutableStateOf() }` for UI-only state

## File Organization Structure

Each module follows a well-defined structure to help with code organization and location:

### Core Structure

- **core/**: Base application structure for business logic and MVI flow standard (not tied to any specific plugin)
- **core/middleware/**: Base application middleware classes
- **core/model/**: Models, typealias, Actions, states and other data types that flow through MVI
- **core/facade/**: Helper classes for specific functions (text copying, data storage, command execution, etc.)
- **core/command/**: ADB command definitions not tied to any specific plugin

### UI Structure

- **ui/**: Generic components, screens, Design System definitions used throughout the system
- **ui/component/**: Primary custom components like buttons, text fields, labels
- **ui/parts/**: Complex custom components like rows, dialogs, menus, selectors and other elements composed of multiple
  primary elements
- **ui/screen/**: Base application screens

### Plugin Structure

- **plugins/**: Plugin-specific code
- **plugins/\<plugin-name\>/ui/component/**: Plugin-specific screen components
- **plugins/\<plugin-name\>/ui/**: Plugin screen(s)
- **plugins/\<plugin-name\>/structure/**: Plugin middleware, facades and reducers
- **plugins/\<plugin-name\>/definition/**: Plugin implementation, actions and plugin-specific models
- **plugins/\<plugin-name\>/di/**: Plugin-specific injection modules

### Dependency Injection

- **di/**: Base application injection modules

### File Organization Rules

- **One class per file**: Each class, interface, model and Compose function (component) must have its own file
- **Descriptive naming**: File names should match the class/component name exactly
- **Package structure**: Follow the directory structure exactly in package declarations
- **No mixed responsibilities**: Each file should contain only one primary entity

Other base application files are placed in the code directory root.

## Testing Patterns

**Test Structure**: Tests in `src/test/kotlin/`
- Unit tests for middleware, reducers, and commands
- Mock dependencies with MockK
- Use `Fixtures.kt` for test data
- AssertK for fluent assertions

**Coverage**: Kover integration excludes UI packages from coverage requirements

## State Management Flow

**Action Dispatch**: Actions flow through the system as:
1. UI dispatches action via `onAction(Action.YourAction(...))`
2. `AppStateManager` receives action and routes to middleware pipeline
3. Middleware intercepts actions for side effects (ADB commands)
4. `AppReducer.reduce()` transforms state based on action
5. UI recomposes via `app.listen().collectAsState()`

**Plugin State Management**: Each plugin manages its data through:
- `WindowResult<T>` stored in `AppState.windows` map (keyed by plugin ID)
- Search terms preserved per plugin
- Plugin-specific actions handled by plugin middleware

## Key Development Notes

- **ADB Path Configuration**: App requires ADB path setting, shows dialog if not configured
- **APK Signature Extraction**: Pure ADB-based signature extraction using local Android SDK tools
- **Android SDK Integration**: Auto-detects Android SDK build-tools for apksigner functionality
- **Temporary File Management**: APK extraction to temp folders with automatic cleanup
- **Error Handling**: Centralized error state in `AppState.errorMessage`
- **Device Management**: Auto-refresh device list, connection status monitoring
- **Command Execution**: All ADB commands extend `AdbCommand<T>` and are executed via `CommandExecutor`

## Common Issues & Solutions

- **Build failures**: Ensure Java 17+ and proper Gradle setup
- **UI rendering issues**: Check Material3 imports vs Material2, avoid hardcoded colors
- **State not updating**: Verify action dispatching and middleware implementation
- **ADB connection problems**: Check device authorization and ADB path configuration
- **Plugin not showing**: Verify plugin is registered in `di/AppModule.kt` PLUGIN_LIST_DEPENDENCY
- Remove the test files. These should never be commited. When you generate them, you must always sanitize after using

## Interaction notes
- When the User ask questions this does not mean something is wrong or needs to change. You just need to check and provide the answer.
- 