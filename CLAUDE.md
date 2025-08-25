# CLAUDE.md

This file provides focused guidance to Claude Code when working with this repository.

## Project Summary

C3PO is a Kotlin + Jetpack Compose Desktop tool for exploring and debugging connected Android devices via ADB. It
features a plugin-based architecture, Redux-style state management, and a multi-module structure.

## Codebase Highlights

- **State Management**: Centralized `AppState`, `Action` sealed classes, middleware interceptors, and reducers. State is
  observed via `app.listen().collectAsState()`.
- **Plugin Architecture**: Each plugin defines its own `Plugin<T>` implementation, middleware, and UI. Plugins are
  registered via DI in `AppModule.kt`.
- **Command Pattern**: ADB operations are encapsulated in command classes and executed through `CommandExecutor`.
- **UI Layer**: Built with Material3 theming, custom components, and modular layout (`sidebar`, `topbar`, `content`).
- **Modules**:
    - `c3po-core`: shared business logic
    - `c3po-desktop`: Compose Desktop UI
    - `c3po-plugin`: Android Studio plugin (optional)

## Interaction Notes

- When the user asks a question, do not assume something is wrong — simply check and respond appropriately.
- Do not alter plugin registration, themes, or layout logic unless explicitly instructed.
- You must not commit test files or example fixtures. If generated, they must be sanitized before committing.