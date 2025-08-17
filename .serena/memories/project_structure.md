# C3PO Project Structure

## Multi-Module Architecture

### c3po-core/

Core business logic shared between desktop and Android Studio plugin

- **core/middleware/**: Base application middleware classes
- **core/model/**: Models, Actions, states, and data types for MVI flow
- **core/facade/**: Helper classes (text copying, data storage, command execution, socket communication)
- **core/command/**: ADB command definitions and base socket instructions

### c3po-desktop/

Desktop UI implementation with Jetpack Compose Desktop

- **ui/**: Generic components, screens, Design System definitions
    - **ui/component/**: Primary custom components (buttons, text fields, labels)
    - **ui/parts/**: Complex custom components (rows, dialogs, menus, selectors)
    - **ui/screen/**: Base application screens
- **plugins/**: Plugin-specific code
    - **plugins/<plugin-name>/ui/**: Plugin screens and UI components
    - **plugins/<plugin-name>/structure/**: Plugin middleware, facades, reducers
    - **plugins/<plugin-name>/definition/**: Plugin implementation, actions, models
- **di/**: Dependency injection modules
- **core/**: Desktop-specific core logic

### c3po-plugin/

Android Studio plugin implementation (shares core module)

## Plugin Structure

Each plugin follows consistent organization:

```
plugins/<plugin-name>/
├── ui/
│   ├── component/     # Plugin-specific UI components
│   └── <PluginName>Screen.kt
├── structure/
│   ├── <PluginName>Middleware.kt
│   └── <PluginName>Facades.kt (if needed)
├── definition/
│   ├── <PluginName>Plugin.kt
│   ├── <PluginName>Actions.kt
│   └── <PluginName>Models.kt
└── di/ (optional)
    └── <PluginName>Module.kt
```

## Existing Plugins

- **device/**: Basic device connection and status
- **activities/**: Launch activities, view activity lists
- **packages/**: Manage installed packages, signatures, lifecycle
- **attrs/**: Device information display
- **services/**: Start/stop services
- **permissions/**: View app permissions
- **signature/**: Analyze application signing and certificates
- **automation/**: Script execution and automation workflows
- **intents/pending/**: Pending intent analysis

## Configuration Files

- **build.gradle.kts**: Root build configuration
- **gradle/libs.versions.toml**: Version catalog for dependencies
- **settings.gradle.kts**: Multi-module project settings
- **CLAUDE.md**: Development guidelines and architecture documentation