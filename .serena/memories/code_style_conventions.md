# C3PO Code Style and Conventions

## File Organization Rules

- **One class per file**: Each class, interface, model and Compose function (component) must have its own file
- **Descriptive naming**: File names should match the class/component name exactly
- **Package structure**: Follow the directory structure exactly in package declarations
- **No mixed responsibilities**: Each file should contain only one primary entity

## Naming Conventions

- **Classes**: PascalCase (e.g., `AppStateManager`, `SocketClient`)
- **Functions**: camelCase (e.g., `startActivity`, `listPackages`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `PLUGIN_LIST_DEPENDENCY`)
- **Packages**: lowercase with dots (e.g., `core.model`, `plugins.activities`)

## Kotlin Style Guidelines

- Use data classes for models
- Prefer sealed classes/interfaces for Actions and state representations
- Use extension functions appropriately
- Follow Kotlin coding conventions

## UI/Compose Conventions

- Always use `MaterialTheme.colorScheme` colors, never hardcoded colors
- Use `Card` components with `RoundedCornerShape(12.dp)` for containers
- Custom components follow naming: `CustomTextField`, `CustomActionButton`
- Component heights: 36dp for text fields, 28dp for action buttons
- Use `@Composable` functions for UI components

## Architecture Patterns

- **Actions**: Sealed interfaces extending `IAction`
- **State**: Immutable data classes
- **Middleware**: Handle side effects and command execution
- **Plugins**: Implement `Plugin<T>` interface
- **Commands**: Extend `AdbCommand<T>` or `EnhancedAdbCommand<T>`

## Testing Conventions

- Test files in `src/test/kotlin/`
- Use MockK for mocking dependencies
- Use AssertK for fluent assertions
- Test data in `Fixtures.kt`
- Unit tests for middleware, reducers, and commands