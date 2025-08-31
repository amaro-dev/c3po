# DEVELOPMENT.md

This document provides guidance for developers contributing to the C3PO Android debugging tool.

---

## Project Setup

### Requirements

- **JDK**: Java 17 or higher
- **Gradle**: Wrapper included
- **ADB**: Ensure it is accessible in your PATH

### Java SDK Configuration (macOS)

1. Check available Java versions:
   ```bash
   /usr/libexec/java_home -V
   ```

2. This project uses Java 17+. Check `local-config.properties` for its path

---

## Common Commands

### Build and Run

```bash
./gradlew build         # Build all modules
./gradlew run           # Run desktop app
./gradlew test          # Run all unit tests
./gradlew koverHtmlReport  # Generate coverage report
./gradlew clean build   # Clean and rebuild
./gradlew packageDmg    # Package macOS .dmg
```

### Module-Specific Builds

```bash
./gradlew :c3po-core:build
./gradlew :c3po-desktop:build
./gradlew :c3po-plugin:build
```

### Running Specific Tests

```bash
./gradlew test --tests "ClassName"
./gradlew test --tests "package.*"
./gradlew :c3po-desktop:test
```

---

## Architecture Overview

### State Management

- **AppState**: Global state container
- **Action**: Sealed classes representing events
- **Middleware**: Executes side effects (e.g., ADB)
- **Reducer**: Updates state based on actions

### Plugin System

- Implements `Plugin<T>` interface
- Includes `middleware`, Compose UI, and action handling
- Registered in `di/AppModule.kt` (`PLUGIN_LIST_DEPENDENCY`)

### Command Pattern

- ADB commands are implemented via `AdbCommand` and `CommandExecutor`
- Examples: `ListPackagesCommand`, `StartActivityCommand`, `ExtractApkCommand`

### Dependency Injection

- Uses **Koin**
- Modules declared in `di/`
- Supports named bindings for plugin collections

---

## UI Guidelines

### Material Design System

- Theme defined in `ui/AndroidGreenTheme.kt`
- Uses Material3 `ColorScheme`
- Light and dark theme supported
- Do not use hardcoded colors

### Layout Structure

- `ui/NewLayout.kt`: Sidebar + topbar + content pattern
- Custom components in `ui/component/`
- Complex composites in `ui/parts/`

### Component Examples

- `CustomTextField`: 36dp height, styled focus states
- `CustomActionButton`: 28dp with interaction states

---

## File & Package Structure

### Core Modules

```
core/
  ├── middleware/     # Middleware for app and plugins
  ├── model/          # Actions, state types, enums
  ├── facade/         # Reusable logic helpers (text copy, command exec, etc.)
  ├── command/        # Core ADB command definitions
```

### UI

```
ui/
  ├── component/      # Buttons, text fields, etc.
  ├── parts/          # Complex components (rows, dialogs, selectors)
  ├── screen/         # App-level screens
```

### Plugins

```
plugins/
  └── <plugin-name>/
       ├── ui/              # Screens
       ├── ui/component/    # Plugin-specific UI elements
       ├── structure/       # Middleware, facades, reducers
       ├── definition/      # Plugin impl, models, actions
       ├── di/              # Plugin-specific Koin modules
```

### Rules

- One class per file
- File name must match class/component name
- Respect package structure
- No mixed responsibilities per file

---

## Testing

### Structure

- Tests live in `src/test/kotlin/`
- Use **MockK** for mocking
- Use **AssertK** for assertions
- `Fixtures.kt` for test data

### Coverage

- Use `./gradlew koverHtmlReport`
- UI files are excluded from coverage requirements

### What/How to test

- Tests should focus the feature, not coverage
- Tests that does not add to the whole purpose, suggest to delete
- For new features, use TDD

### After finishing your development

- Run the tests to guarantee that they pass
- Run the app to check if it compile
- We have MCPs in place for instrumentation of the screen. It should be enough for you to test the features and bug
  fixes you made

---

## Common Issues & Fixes

| Issue                | Solution                                                          |
|----------------------|-------------------------------------------------------------------|
| Build failure        | Ensure Java 17+ is used and Gradle is set up properly             |
| UI not rendering     | Check usage of Material3 components and avoid hardcoded colors    |
| State not updating   | Confirm correct dispatch and middleware handling                  |
| ADB not working      | Verify ADB path and device authorization                          |
| Plugin not showing   | Ensure plugin is registered in `AppModule.kt` and has a proper ID |
| Test files committed | Do **not** commit generated test files. Clean them before pushing |

---

## Notes

- APK signatures are extracted using the local Android SDK (`apksigner`)
- All ADB commands are wrapped in `AdbCommand<T>` implementations
- APKs are temporarily extracted and auto-cleaned
- Errors are stored in `AppState.errorMessage`
- Device list refreshes automatically
