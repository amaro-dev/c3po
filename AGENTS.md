# AGENTS.md — Agent-Agnostic Engineering Guide

This repository uses Kotlin + Jetpack Compose Desktop, a plugin-based architecture, and a Redux-style state management
pattern (via dev.amaro.sonic). This guide is the canonical, tool-agnostic reference for any AI agent or contributor
adding features to C3PO. User-facing documentation lives under `docs/` and should not be mixed with this engineering
guide.

## Architecture Overview

- App State and Actions
    - `AppState` holds devices, the selected device, current plugin, per-plugin windows (
      `Map<String, WindowResult<*>>`), messages, update state, and settings.
    - `Action` is a sealed hierarchy in `core/model/Actions.kt`. UI and middlewares dispatch actions; `AppReducer`
      updates `AppState` deterministically.
    - `AppStateManager` wires reducers and middlewares and emits `Action.UpdatedState(old, new)` after each reduction.

- Plugins
    - Implement `plugins.Plugin<T>` with `id`, `name`, `icon`, `middleware`, and `present()` UI.
    - Define a sealed `Actions` interface inside the plugin for its events; mark ADB-backed ones as
      `Action.CommandAction`.
    - `PluginSelectorMiddleware` routes `Action.StartPlugin`, defers startup until settings and a device are ready, and
      forwards plugin-responsible actions to each plugin’s middleware (`isResponsibleFor`).

- ADB Commands
    - Implement `AdbCommand<T>` with `command: String` and `parse(result: String): T` under
      `c3po-core/src/main/kotlin/core/command`.
    - Execute via `CommandExecutor.go(command, adbPath, device?)`.
    - In plugin middlewares, prefer `PluginMiddleware.execute(cmd, state, executor)` to propagate adb path and device
      automatically.

- Middlewares
    - Cross-cutting middlewares: `DeviceMiddleware`, `SettingsMiddleware`, `UpdateMiddleware`, `USBMonitorMiddleware`,
      `LoggingMiddleware`, `RestartMiddleware`.
    - Plugin middlewares live under `plugins/<name>/structure` and extend `plugins.PluginMiddleware`.
    - Use `AsyncMiddlewareBase<AppState>` for async/I-O work; use `IMiddleware<AppState>` (or direct middleware) for
      trivial state-only handling.

- UI Layer
    - Compose Desktop with Material3. Shared UI components under `ui/parts` and `ui/component`.
    - Per-plugin UI is implemented in the plugin’s `present(...)` method.

## Conventions

- Plugin IDs are short UPPERCASE strings, e.g., `"DEVICE"`, `"PACKAGES"`.
- Register plugins in `di/AppModule.kt` under `PLUGIN_LIST_DEPENDENCY`.
- Use `Action.DeliverPluginResult(pluginId, items)` to populate
  `windows[pluginId] = WindowResult(searchTerm, items, filterState)` and select the plugin.
- Persist search term and per-plugin filter state with `Action.ChangeFilter` and `Action.UpdatePluginFilters`.
- Mark any ADB-backed action as `Action.CommandAction` to automatically:
    - Enforce ADB path presence (via `DeviceMiddleware`).
    - Set loading state (`CommandStatus.Running`) for a consistent UI.

## Logging

- Use `StructuredLogger.getInstance()` for non-blocking, structured logs.
- Logging behavior is controlled by settings (see `Settings.kt`):
    - `logging.enabled`, `logging.adb` ("off" | "errors" | "full"), `logging.perform`, `logging.reduce`.
    - `LoggingMiddleware` logs performed and reduced actions when enabled.

## Feature Design Checklist

Before implementing a feature, answer these questions to map the work into the MVI flow correctly.

1) User Interactions

- Actions: How many, and which `Action`s represent each user interaction on screen?
- Feedback: What feedback is expected (loading pill, success toast, errors)?
- Operation State: Do we need to control operation state in the UI?
- ADB Command: Does any `Action` execute an ADB command? If yes, mark it as `Action.CommandAction`.

2) Responsibility Mapping

- Scope: Is the `Action` app-wide or plugin-specific?
- Existing Middleware: Is there an existing middleware that already handles it?
- State-only: Does it only mutate state? If so, prefer reducer-only handling or put it into a direct middleware (
  `ConditionedDirectMiddleware` in `AppModule.kt`).

3) Triggered Operations

- Availability: Does the operation already exist (command/facade)?
- Similarity: Is there a class handling similar operations to reuse?
- Failure: Can it fail? Define error handling and user messaging.
- Duration: Is it long-running? Consider async middleware and progress updates.

4) Expected Results

- Side Effects: Does it produce side effects (files, device state changes)?
- Output Actions: Which `Action`s deliver results and state transitions (e.g., `DeliverPluginResult`, `SetSuccess`,
  error/progress actions)?
- Reducer Behavior: How should `AppReducer` apply these output actions (windows, messages, flags)?

These decisions guide:

- Middleware Type: Synchronous vs asynchronous.
- Dispatch Method: When to `processor.reduce`, `processor.perform`, or `schedule` (see below).
- End-to-end MVI flow.

## Dispatching: reduce vs perform vs schedule

- `processor.reduce(...)`: Apply a state change now (e.g., deliver results, toggle flags, set messages).
- `processor.perform(...)`: Dispatch a follow-up action to go through the middleware chain (e.g., after success, refresh
  or chain operations).
- Scheduling: Use when a follow-up must occur strictly after the current reduction. `AppStateManager` implements
  `IActionScheduler` and drains scheduled actions post-reduction. Prefer `perform(...)` unless ordering requires
  scheduling.

## Choosing Middleware

- Direct/Sync: Use `ConditionedDirectMiddleware` (see `AppModule.kt`) for pure state changes that don’t require I/O.
- Async: Extend `AsyncMiddlewareBase<AppState>` for ADB operations, I/O, retries, or progress.

## Typical MVI Flow

1. UI dispatches an action (often plugin-specific). If it’s an ADB operation, implement `Action.CommandAction`.
2. Middleware handles the action:
    - Set running state for `CommandAction`.
    - Execute `AdbCommand` via `CommandExecutor` or `PluginMiddleware.execute`.
    - On success: `Action.DeliverPluginResult` and/or `Action.SetSuccess`; optionally `perform(...)` for follow-ups.
    - On failure: `Action.SetCommandError`.
3. `AppReducer` updates state: windows, filters, messages, flags.
4. UI observes state via `collectAsState()` and renders feedback.

## Example: Minimal Plugin

```kotlin
// definition
class FilesPlugin(
    executor: CommandExecutor
) : plugins.Plugin<DeviceFile> {
    sealed interface Actions : IAction {
        data object List : Actions, Action.CommandAction
        data class Delete(val path: String) : Actions, Action.CommandAction
    }

    override val id = "FILES"
    override val name = "Files"
    override val icon = Icons.Default.Folder
    override val middleware: IMiddleware<AppState> = FilesPluginMiddleware(id, executor)
    override fun isResponsibleFor(action: IAction) = action is Actions

    @Composable
    override fun present(result: WindowResult<DeviceFile>, onAction: OnAction) {
        val items = result.result
        val search = result.searchTerm
        Column {
            EnhancedSearchBar(searchTerm = search, onSearchChange = { onAction(Action.ChangeFilter(id, it)) })
            EnhancedScrollableList(items = items) { file, _ -> /* row */ }
        }
    }
}

// middleware
class FilesPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin, is FilesPlugin.Actions.List -> {
                execute(ListFilesCommand(), state, executor).handle(processor) { files ->
                    processor.reduce(Action.DeliverPluginResult(pluginName, files))
                }
            }
            is FilesPlugin.Actions.Delete -> {
                execute(RemoveDeviceFileCommand(action.path), state, executor)
                    .onSuccess { processor.perform(FilesPlugin.Actions.List) }
                    .handle(processor)
            }
        }
    }
}
```

## User feedback success/error - Command Status Handling

When working with command lifecycle and UI loading/error feedback, follow these strict rules:

- Never reduce `Action.SetCommandCompleted` after reducing `Action.SetCommandError` for the same operation. An error
  must leave `commandStatus = Failed` visible to the UI.
- Prefer `Result.handle(processor)` for command results:
    - On success: it reduces `SetCommandCompleted` automatically.
    - On failure: it reduces only `SetCommandError` (do not add `SetCommandCompleted`).
- Only dispatch `SetCommandCompleted` explicitly on clear success paths that are not using `handle(...)`.
- For ADB-backed actions, always mark them as `Action.CommandAction` so `DeviceMiddleware` enforces ADB path and sets
  `SetCommandRunning` consistently.
- Do not overwrite `Failed` with `Completed` as a way to “clear loading” — the error state already clears loading and
  must remain until `Action.ClearError`.

## Composite Command Pattern

Some features require multiple ADB calls to build a single UI result (e.g., list activities + query which are
launcher-capable).

- Where to implement: In plugin middleware (extend `AsyncMiddlewareBase<AppState>`), not in `parse(...)`.
- Parallelism: Launch sub-commands in parallel using coroutines (`coroutineScope { async { ... } }`) to reduce latency.
- Status handling:
    - Avoid calling `Result.handle(processor)` per sub-command; aggregate and set status once for the overall operation.
    - If an essential sub-command fails, reduce only `Action.SetCommandError(message)` and do not send
      `SetCommandCompleted`.
    - If a non-essential enrichment fails, deliver base results and then reduce `SetCommandCompleted`.
- Device/ADB propagation: Always use `PluginMiddleware.execute(cmd, state, executor)` so `adbPath` and selected device
  are honored.
- Parsing purity: Keep `AdbCommand.parse(result)` side-effect free; no ADB/I-O inside `parse`.

Example sketch:

```
coroutineScope {
  val baseDef = async { execute(BaseCommand(), state, executor) }
  val enrichDef = async { execute(EnrichCommand(), state, executor) }

  val baseRes = baseDef.await()
  if (baseRes.isFailure) {
    processor.reduce(Action.SetCommandError(baseRes.exceptionOrNull()?.message ?: "Base command failed"))
    return@coroutineScope
  }

  val base = baseRes.getOrThrow()
  val enrich = enrichDef.await().getOrNull()
  val merged = merge(base, enrich)
  processor.reduce(Action.DeliverPluginResult(pluginId, merged))
  processor.reduce(Action.SetCommandCompleted)
}
```

## Common Pitfalls

- Forgetting to mark ADB-backed actions as `Action.CommandAction` (breaks loading and ADB checks).
- Bypassing `DeliverPluginResult` and directly mutating windows (loses search/filter persistence and selection).
- Double-reducing completion after using `Result.handle(...)` (it already reduces `SetCommandCompleted` on success).
- Triggering plugin start before settings/device are ready (use `Action.StartPlugin`, `PluginSelectorMiddleware` handles
  deferral).

## Documentation Updates

When adding or changing user-facing functionality, update the end-user documentation under `docs/` (GitHub Pages
source):

- Add or edit relevant pages in `docs/wiki/` and ensure `docs/index.md` links are current.
- Keep language user-focused (what it does, how to use it, screenshots if helpful).
- Do not mix agent/developer guidance with user docs — engineering guidance stays in `AGENTS.md` and `.claude/`.
- If the feature impacts onboarding or settings, update the "Getting Started" or related guides accordingly.

## Scope & Non-Goals

- This guide is for contributors/agents. End-user documentation is under `docs/` and published to GitHub Pages.
- Claude-specific behaviors live in `CLAUDE.md`. `.claude/` contains Claude helper docs (env, prompts, testing, UI
  tips).
