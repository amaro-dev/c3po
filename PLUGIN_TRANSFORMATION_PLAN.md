# C3PO Plugin Transformation Plan

## Overview

Transform C3PO into a multi-platform solution with shared business logic:
- **Standalone Desktop App** (existing, enhanced)
- **Android Studio Plugin** (new)
- **Shared Core Library** (extracted from existing code)

## Multi-Module Project Structure

```
c3po/
├── c3po-core/                    # Shared business logic
│   ├── src/main/kotlin/
│   │   ├── commands/             # ADB commands (100% shareable)
│   │   ├── models/               # Data models (100% shareable)
│   │   ├── plugins/              # Plugin business logic (95% shareable)
│   │   │   ├── packages/         # Package management logic
│   │   │   ├── activities/       # Activity management logic
│   │   │   ├── services/         # Service management logic
│   │   │   ├── permissions/      # Permission analysis logic
│   │   │   └── core/             # Plugin interfaces & base classes
│   │   ├── socket/               # Socket communication (100% shareable)
│   │   ├── facade/               # External interfaces (100% shareable)
│   │   ├── device/               # Device management (95% shareable)
│   │   └── utils/                # Utilities and extensions (100% shareable)
│   └── build.gradle.kts
│
├── c3po-desktop/                 # Standalone desktop application
│   ├── src/main/kotlin/
│   │   ├── ui/                   # Compose Desktop UI components
│   │   │   ├── screens/          # Main screens and layouts
│   │   │   ├── components/       # Reusable UI components
│   │   │   ├── theme/            # Material Design theme
│   │   │   └── plugins/          # Plugin UI presentations
│   │   ├── core/                 # Desktop-specific state management
│   │   │   ├── state/            # Redux-like state management
│   │   │   ├── middleware/       # Desktop middleware pipeline
│   │   │   └── di/               # Dependency injection for desktop
│   │   ├── lifecycle/            # Desktop app lifecycle
│   │   └── Main.kt               # Desktop application entry point
│   ├── src/main/resources/       # Desktop resources (icons, APKs)
│   └── build.gradle.kts
│
├── c3po-plugin/                  # Android Studio plugin
│   ├── src/main/kotlin/
│   │   ├── ui/                   # IntelliJ UI components
│   │   │   ├── toolwindows/      # Tool window implementations
│   │   │   ├── dialogs/          # Plugin dialogs and popups
│   │   │   ├── components/       # Swing/IntelliJ UI components
│   │   │   └── plugins/          # Plugin UI adapters
│   │   ├── actions/              # IDE actions and menu items
│   │   ├── services/             # IDE services and components
│   │   ├── integration/          # IDE and project integration
│   │   ├── settings/             # Plugin settings and configuration
│   │   └── lifecycle/            # Plugin lifecycle management
│   ├── src/main/resources/
│   │   ├── META-INF/
│   │   │   └── plugin.xml        # Plugin descriptor
│   │   └── icons/                # Plugin icons
│   └── build.gradle.kts
│
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Multi-module settings
└── gradle.properties             # Shared properties
```

## Phase 1: Core Extraction (Week 1-2)

### 1.1 Create Core Module
```bash
# New module structure
mkdir c3po-core
mkdir c3po-core/src/main/kotlin
```

### 1.2 Extract Shareable Components

**Commands Package** (100% shareable)
- Move entire `commands/` package to `c3po-core`
- No modifications needed - pure business logic

**Models Package** (100% shareable)
- Move entire `models/` package to `c3po-core`
- Data classes are platform-agnostic

**Socket Communication** (100% shareable)
- Move `socket/` package to `c3po-core`
- Socket client is platform-independent

**Facade Package** (100% shareable)
- Move `facade/` package to `c3po-core`
- External interfaces and companion management

### 1.3 Abstract Plugin System

**Create Plugin Abstraction**
```kotlin
// c3po-core/src/main/kotlin/plugins/core/
interface PluginCore<T> {
    val id: String
    val name: String
    fun isResponsibleFor(action: IAction): Boolean
    suspend fun executeAction(action: IAction): Result<T>
}

abstract class AbstractPlugin<T> : PluginCore<T> {
    // Shared business logic
    abstract val commandExecutor: CommandExecutor
    abstract fun parseResult(data: String): T
}
```

**Platform-Specific Plugin Interface**
```kotlin
// Desktop plugins extend this
interface DesktopPlugin<T> : PluginCore<T> {
    @Composable
    fun present(result: WindowResult<T>, onAction: OnAction)
}

// IDE plugins extend this
interface IDEPlugin<T> : PluginCore<T> {
    fun createToolWindow(result: WindowResult<T>): JComponent
}
```

## Phase 2: Desktop App Refactoring (Week 3)

### 2.1 Update Desktop Module Dependencies
```kotlin
// c3po-desktop/build.gradle.kts
dependencies {
    implementation(project(":c3po-core"))
    implementation(compose.desktop.currentOs)
    // ... other desktop-specific dependencies
}
```

### 2.2 Adapt Desktop Plugins
```kotlin
// c3po-desktop/src/main/kotlin/plugins/
class DesktopPackagesPlugin(
    private val corePlugin: PackagesPluginCore
) : DesktopPlugin<AppPackage> by corePlugin {

    @Composable
    override fun present(result: WindowResult<AppPackage>, onAction: OnAction) {
        // Compose Desktop UI implementation
    }
}
```

### 2.3 Desktop State Management
- Keep existing Redux-like pattern
- Adapt to use core business logic
- Maintain current UI behavior

## Phase 3: Android Studio Plugin Development (Week 4-6)

### 3.1 Plugin Descriptor
```xml
<!-- c3po-plugin/src/main/resources/META-INF/plugin.xml -->
<idea-plugin>
    <id>dev.amaro.c3po.plugin</id>
    <name>C3PO Android Explorer</name>
    <version>2.0.1</version>
    <vendor email="support@amaro.dev" url="https://amaro.dev">Amaro Dev</vendor>

    <description><![CDATA[
        Android device exploration and debugging tool integrated into Android Studio.
        Provides comprehensive package management, activity launching, service control,
        and permission analysis directly within your development environment.
    ]]></description>

    <depends>com.intellij.modules.platform</depends>
    <depends>org.jetbrains.android</depends>

    <extensions defaultExtensionNs="com.intellij">
        <toolWindow id="C3PO" secondary="false" anchor="right"
                   factoryClass="ui.toolwindows.C3POToolWindowFactory"/>
        <applicationService serviceImplementation="services.C3POApplicationService"/>
    </extensions>

    <actions>
        <group id="C3PO.ToolbarActions" text="C3PO Actions" popup="true">
            <action id="C3PO.RefreshDevices"
                   class="actions.RefreshDevicesAction"
                   text="Refresh Devices"/>
            <action id="C3PO.ConnectDevice"
                   class="actions.ConnectDeviceAction"
                   text="Connect Device"/>
        </group>
    </actions>
</idea-plugin>
```

### 3.2 Tool Window Implementation
```kotlin
// c3po-plugin/src/main/kotlin/ui/toolwindows/
class C3POToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val mainPanel = C3POMainPanel(project)

        val content = ContentFactory.SERVICE.getInstance()
            .createContent(mainPanel, "Devices", false)
        contentManager.addContent(content)
    }
}

class C3POMainPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val deviceSelector = DeviceSelector()
    private val pluginTabs = JTabbedPane()

    init {
        setupUI()
        initializePlugins()
    }

    private fun initializePlugins() {
        // Add plugin tabs
        pluginTabs.addTab("Packages", IDEPackagesPlugin().createUI())
        pluginTabs.addTab("Activities", IDEActivitiesPlugin().createUI())
        pluginTabs.addTab("Services", IDEServicesPlugin().createUI())
        pluginTabs.addTab("Permissions", IDEPermissionsPlugin().createUI())
        // ... other plugins
    }
}
```

### 3.3 IDE Plugin Adapters
```kotlin
// c3po-plugin/src/main/kotlin/plugins/
class IDEPackagesPlugin(
    private val corePlugin: PackagesPluginCore
) : IDEPlugin<AppPackage> by corePlugin {

    override fun createToolWindow(result: WindowResult<AppPackage>): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            // Search field
            add(JTextField().apply {
                document.addDocumentListener(object : DocumentListener {
                    override fun insertUpdate(e: DocumentEvent) = filterPackages()
                    override fun removeUpdate(e: DocumentEvent) = filterPackages()
                    override fun changedUpdate(e: DocumentEvent) = filterPackages()
                })
            })

            // Package list
            val listModel = DefaultListModel<AppPackage>()
            result.result.forEach { listModel.addElement(it) }

            val packageList = JList(listModel).apply {
                cellRenderer = PackageListCellRenderer()
                selectionMode = ListSelectionModel.SINGLE_SELECTION
            }

            add(JScrollPane(packageList))
        }
    }
}
```

### 3.4 IDE Integration Features

**Project Context Integration**
```kotlin
class AndroidProjectIntegration(private val project: Project) {
    fun getCurrentAppPackage(): String? {
        return AndroidFacet.getInstance(project)
            ?.manifest
            ?.`package`
            ?.value
    }

    fun getConnectedDevices(): List<IDevice> {
        return AndroidDebugBridge.getBridge()
            ?.devices
            ?.toList() ?: emptyList()
    }
}
```

**IDE Actions**
```kotlin
class InstallCurrentAppAction : AnAction("Install Current App") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val apkPath = getBuiltApkPath(project)

        // Use shared core logic
        val installCommand = InstallApkCommand(apkPath)
        // Execute through shared command system
    }
}
```

## Phase 4: Enhanced Features (Week 7-8)

### 4.1 IDE-Specific Enhancements

**Build Integration**
- Install/test current project APK
- Automatic device detection from IDE
- Integration with Android project structure

**Enhanced Debugging**
- Launch activities with IDE debugger attachment
- Logcat integration
- Breakpoint-aware service management

**Project Context**
- Highlight current project package
- Quick actions for current app
- Manifest integration

### 4.2 Desktop App Enhancements

**Standalone Features**
- Multiple project support
- APK analyzer integration
- Device farm management

## Implementation Strategy

### Dependencies & Build Configuration

**Root build.gradle.kts**
```kotlin
subprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
```

**Core Module Dependencies**
```kotlin
// c3po-core/build.gradle.kts
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("dev.amaro:sonic:0.5.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.13.12")
}
```

**Plugin Module Dependencies**
```kotlin
// c3po-plugin/build.gradle.kts
plugins {
    id("org.jetbrains.intellij") version "1.17.2"
}

dependencies {
    implementation(project(":c3po-core"))
}

intellij {
    version.set("2023.2.5")
    type.set("AI") // Android Studio
    plugins.set(listOf("android"))
}
```

### Migration Steps

1. **Week 1**: Extract core business logic to shared module
2. **Week 2**: Refactor desktop app to use shared core
3. **Week 3**: Test desktop app with new architecture
4. **Week 4**: Create basic Android Studio plugin structure
5. **Week 5**: Implement core plugin functionality
6. **Week 6**: Add IDE-specific integrations
7. **Week 7**: Enhanced features and polish
8. **Week 8**: Testing and documentation

### Benefits of Plugin Version

**Developer Workflow Integration**
- No context switching between tools
- Direct integration with project structure
- Seamless debugging workflow

**Enhanced Functionality**
- Access to IDE APIs and project information
- Integration with existing Android development tools
- Automatic device detection and management

**Distribution & Updates**
- JetBrains Marketplace distribution
- Automatic updates through IDE
- Better discoverability for Android developers

## Testing Strategy

### Shared Core Testing
- Unit tests remain unchanged
- Integration tests for platform abstraction
- Mock platform-specific components

### Platform-Specific Testing
- Desktop: Compose UI testing
- Plugin: IntelliJ testing framework
- End-to-end testing for both platforms

## Future Considerations

### Additional Platforms
- **VSCode Extension**: Using same core logic
- **Web Interface**: Browser-based version
- **CLI Tool**: Command-line interface

### Enhanced Sharing
- **UI Components**: Abstract UI patterns
- **Settings Sync**: Cross-platform configuration
- **Plugin Marketplace**: Community plugins for both platforms

---

This transformation leverages C3PO's excellent architecture to create a powerful multi-platform solution while maximizing code reuse and maintaining feature parity across platforms.
