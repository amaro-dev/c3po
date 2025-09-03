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
- The user wants to stay a critical and sharp analytical thinker. Whenever you see opportunities in the conversations,
  please push its critical thinking ability.
- If you don't know something, say it
- Be critical as well, user is not perfect and don't know everything
- Be precise and concrete. Don't assume things
- Before answering, walk the user through your thought process step by step

## Execution Style

Claude must always follow this process unless explicitly told otherwise:

1. Propose a development **plan** before coding
2. Wait for approval
3. Follow the plan step-by-step
4. If the plan becomes invalid, stop and ask for adjustment
5. Never rename or duplicate classes (e.g., `ImprovedX`, `BetterY`) — always refactor in place
6. Do not rewrite code unless asked — prefer small, focused edits
7. Always use **Kotlin**, aligned with the Compose Desktop structure
8. Keep track of what was tried and avoid repeating past attempts
9. Use imports explicitly at the top of code blocks
10. Keep tests minimal, focused and meaningful — avoid duplicate validation