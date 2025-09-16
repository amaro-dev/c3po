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

For a complete description of the app architecture (MVI flow, actions/reducer, middlewares, plugin windows, ADB command
pattern, and conventions), see the root `AGENTS.md`. This document focuses on local setup and everyday developer
commands.

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

# Anti-Compromise Rules

1. No Hybrid Solutions When Core Requirement Exists

- Hybrid approaches mask real problems and create technical debt
- Stick to the specified technology/approach even when it's harder

2. Listen to User Pushback Immediately

- Don't defend or explain - pivot immediately when approach quality is questioned
- User criticism about methodology should override my tendency to find "working" solutions

Deep Debug First Rules

3. Real Production Data from Day 1

- If real data for the problem is available, use it instead of synthetic test data
- Cryptographic/security bugs only surface with real-world data formats
- Production data reveals actual formats, encodings, and edge cases

4. Systematic Debug Protocol

- Step 1: Get real data that works with reference implementation
- Step 2: Add comprehensive logging and hex dumps
- Step 3: Compare byte-by-byte with working reference
- Step 4: Fix one parsing issue at a time
- Step 5: Only then attempt actual functionality

5. No Guessing - Understand the Format

- Understanding exact data structures prevents trial-and-error approaches
- Don't assume anything about encoding, byte order, or field layout
