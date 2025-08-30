# STRATEGY.md

This document defines architectural and coding strategies used in the C3PO project.

---

## Refactoring Rules

- Always refactor existing code in place — do not create duplicate or "Improved" versions
- Changes should be incremental and reversible
- Use sealed classes and scoped actions for MVI flow
- Follow the naming conventions used in the module
- Do not rewrite plugins unless specifically instructed
- Always clean the code you wrote at the end to remove eventual dead/unused code

---

## Testing Strategy

- Unit tests must be minimal and focused
- Avoid over-testing trivial functions or repeating test logic
- Use `MockK` and `AssertK` only when needed
- Prefer integration of new behavior into existing test suites

---

## Development Rules

- Plugins must follow structure in `plugins/<name>/`
- Use `WindowResult<T>` pattern to manage plugin state
- Register plugins via DI in `AppModule.kt`
- Follow Material3 guidelines for UI
- Do not hardcode colors — use `MaterialTheme.colorScheme`
