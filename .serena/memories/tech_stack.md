# C3PO Technology Stack

## Core Technologies

- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose Desktop 1.7.3
- **Build System**: Gradle with Kotlin DSL
- **State Management**: Sonic library (dev.amaro:sonic:0.5.1) - Redux-style
- **Dependency Injection**: Koin 4.0.2
- **Testing**: JUnit 5, MockK, AssertK, Kotlinx Coroutines Test
- **Coverage**: Kover (Kotlin code coverage)

## Key Dependencies

- **Material Design**: Material3 for Desktop (1.5.0)
- **Icons**: Material Icons Extended for Desktop
- **Menu Components**: Composables UI Menu (1.4.0)
- **Error Tracking**: Sentry JVM (currently commented out)

## Architecture Patterns

- **Redux/MVI**: Centralized state management with Actions, State, and Middleware
- **Command Pattern**: ADB operations encapsulated in command classes
- **Plugin Architecture**: Extensible system with Plugin interface
- **Socket Communication**: Custom client for companion service
- **Multi-Module**: Core business logic separated from UI implementations

## Development Tools

- **Package Management**: macOS DMG packaging support
- **Code Quality**: Kover for test coverage reporting
- **Platform**: Primary focus on macOS (Darwin)