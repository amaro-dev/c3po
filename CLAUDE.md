# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

C3PO is a desktop Android debugging and exploration tool built with Kotlin and Jetpack Compose Desktop. It connects to Android devices via ADB and provides a plugin-based architecture for exploring apps, activities, services, permissions, and more.

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

## Architecture Overview

### Core Components

**Redux-Style State Management**: The app uses a centralized state management pattern with:
- `AppState`: Single source of truth containing device list, current device, plugin state, etc.
- `Action`: Sealed classes defining all possible state changes
- `AppStateManager`: Handles action dispatching and state updates
- `Middleware`: Intercepts actions for side effects (ADB commands, socket communication, etc.)

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
- Examples: `ListPackagesCommand`, `StartActivityCommand`, `ListActivitiesCommand`
- Execution: `CommandExecutor` with strategy pattern for different environments

**Socket Communication**: Custom socket client for companion service communication:
- `SocketClient`: Main interface for socket operations
- `SocketDriver`: Low-level socket handling
- `SocketResponseAggregator`: Handles multi-part responses

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
- TopBar: Device selector and companion status
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
3. Middleware intercepts actions for side effects (ADB commands, socket operations)
4. `AppReducer.reduce()` transforms state based on action
5. UI recomposes via `app.listen().collectAsState()`

**Plugin State Management**: Each plugin manages its data through:
- `WindowResult<T>` stored in `AppState.windows` map (keyed by plugin ID)
- Search terms preserved per plugin
- Plugin-specific actions handled by plugin middleware

## Key Development Notes

- **ADB Path Configuration**: App requires ADB path setting, shows dialog if not configured
- **Companion Service**: Optional Android service for enhanced features, handles installation/connection
- **Socket Communication**: Port forwarding on 9999 for companion service  
- **Error Handling**: Centralized error state in `AppState.errorMessage`
- **Device Management**: Auto-refresh device list, connection status monitoring
- **Command Execution**: All ADB commands extend `AdbCommand<T>` and are executed via `CommandExecutor`

## Common Issues & Solutions

- **Build failures**: Ensure Java 17+ and proper Gradle setup
- **UI rendering issues**: Check Material3 imports vs Material2, avoid hardcoded colors
- **State not updating**: Verify action dispatching and middleware implementation
- **ADB connection problems**: Check device authorization and ADB path configuration
- **Plugin not showing**: Verify plugin is registered in `di/AppModule.kt` PLUGIN_LIST_DEPENDENCY