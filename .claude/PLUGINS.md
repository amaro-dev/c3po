# PLUGINS.md

This document describes how to create, register, and maintain plugins in the C3PO Android debugging tool.

---

## What is a Plugin?

See `AGENTS.md` for the canonical architecture and conventions (plugin responsibilities, `Plugin<T>` contract,
`Action.CommandAction`, `DeliverPluginResult`, filter persistence, and DI registration in `AppModule.kt`).

This document focuses on practical scaffolding and examples to build plugin UIs and middlewares efficiently.

---

## Plugin Folder Structure

Each plugin should reside in its own directory:

```
plugins/<plugin-name>/
├── definition/       # Plugin class, actions, models
├── structure/        # Middleware, reducers, facades
├── ui/               # Screens
├── ui/component/     # Reusable plugin-specific UI elements
├── di/               # DI module (optional)
```

---

## Creating a New Plugin

### 1. Create the plugin class

```kotlin
class YourPlugin : Plugin<YourData> {
    override val id = "your_plugin"
    override val name = "Your Plugin"
    override val icon = Icons.Filled.YourIcon
    override val middleware = YourPluginMiddleware()

    override fun isResponsibleFor(action: IAction): Boolean =
        action is YourPluginAction

    @Composable
    override fun present(
        result: WindowResult<YourData>,
        onAction: OnAction
    ) {
        YourPluginScreen(result.data, onAction)
    }
}
```

### 2. Define your actions

Create a sealed interface extending `IAction`, e.g.:

```kotlin
sealed interface YourPluginAction : IAction {
    data class DoSomething(val param: String) : YourPluginAction
}
```

### 3. Implement middleware

Handle actions and run ADB commands:

```kotlin
class YourPluginMiddleware : PluginMiddleware {
    override suspend fun intercept(
        action: IAction,
        dispatch: Dispatcher,
        getState: () -> AppState
    ) {
        if (action is YourPluginAction.DoSomething) {
            // Execute ADB command here
        }
    }
}
```

### 4. Add UI

Use Compose components to render your plugin UI. Use `MaterialTheme.colorScheme` and shared styles.

---

## Plugin Registration

### Register in AppModule.kt

In `di/AppModule.kt`, add your plugin to the `PLUGIN_LIST_DEPENDENCY`:

```kotlin
factory(Named("PLUGIN_LIST_DEPENDENCY")) {
    listOf(
        DevicePlugin(),
        ActivitiesPlugin(),
        YourPlugin(), // ← add here
        ...
    )
}
```

> 📌 The order defines how plugins appear in the sidebar.

---

## Best Practices

- Use icons from `Icons.Filled` or create your own `ImageVector`
- Plugin IDs must be unique and stable
- Handle long operations with coroutines (`dispatch(Action.Loading(true))`)
- Compose UI must be responsive and visually consistent with the rest of the app
- Don't access global state directly — use `getState()` from middleware

---

## Example Plugins

- **Device**: Device connection, ADB status, authorization
- **Activities**: List and launch activities
- **Packages**: View installed apps, manage lifecycle
- **Permissions**: Inspect granted permissions
- **Services**: Start/stop foreground/background services
- **Signature**: Extract and analyze APK signing info
- **Automation**: Execute scripted automation flows

---

## Common Issues

| Problem                   | Solution                                                       |
|---------------------------|----------------------------------------------------------------|
| Plugin not showing in UI  | Ensure it is registered in `AppModule.kt` and has a valid ID   |
| Middleware not triggered  | Check `isResponsibleFor()` and action dispatch flow            |
| UI not updating           | Make sure to use `WindowResult<T>` and observe state correctly |
| ADB command not executing | Ensure proper coroutine use and ADB setup                      |
