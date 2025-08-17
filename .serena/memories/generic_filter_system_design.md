# Generic Filter System Design for Plugin Pattern

## Current Architecture Analysis

### Current Filter System

- **WindowResult**: Only contains `searchTerm: String`
- **Action.ChangeFilter**: Only handles search term updates
- **AppReducer**: Updates search term in WindowResult
- **Plugin Pattern**: Each plugin manages its own filtering logic in `present()` method

### Current Flow

1. User types in search → `Action.ChangeFilter(pluginName, searchTerm)`
2. AppReducer updates `WindowResult.searchTerm` for plugin
3. Plugin's `present()` method receives updated `WindowResult`
4. Plugin applies local filtering logic using `searchTerm`

## Proposed Generic Filter System

### 1. Enhanced WindowResult

```kotlin
data class WindowResult<out T>(
    val searchTerm: String,
    val result: List<T>,
    val filters: Map<String, Any> = emptyMap() // Generic filter storage
)
```

### 2. Enhanced Actions

```kotlin
// Keep existing ChangeFilter for backward compatibility
data class ChangeFilter(
    val pluginName: String,
    val searchTerm: String,
) : Action

// New generic filter action
data class UpdatePluginFilter(
    val pluginName: String,
    val filterKey: String,
    val filterValue: Any
) : Action

// Batch filter update action
data class UpdatePluginFilters(
    val pluginName: String,
    val filters: Map<String, Any>
) : Action
```

### 3. Enhanced AppReducer

```kotlin
is Action.UpdatePluginFilter ->
    currentState.copy(
        windows = currentState.windows.update(action.pluginName) { windowResult ->
            windowResult.copy(
                filters = windowResult.filters + (action.filterKey to action.filterValue)
            )
        }
    )

is Action.UpdatePluginFilters ->
    currentState.copy(
        windows = currentState.windows.update(action.pluginName) { windowResult ->
            windowResult.copy(
                filters = action.filters
            )
        }
    )
```

### 4. Plugin-Specific Filter Definitions

Each plugin defines its own filter types:

```kotlin
// Activities Plugin
object ActivitiesFilters {
    const val LAUNCHABLE = "launchable"
    const val DEBUGGABLE = "debuggable"
}

// Packages Plugin  
object PackagesFilters {
    const val SYSTEM_APPS = "systemApps"
    const val USER_APPS = "userApps"
    const val ENABLED = "enabled"
    const val DEBUGGABLE = "debuggable"
    const val HAS_SIGNATURE = "hasSignature"
}
```

### 5. Generic Filter Helper Functions

```kotlin
// Extension functions for WindowResult
fun <T> WindowResult<T>.getBooleanFilter(key: String, default: Boolean = false): Boolean =
    (filters[key] as? Boolean) ?: default

fun <T> WindowResult<T>.getStringFilter(key: String, default: String = ""): String =
    (filters[key] as? String) ?: default

fun <T> WindowResult<T>.getIntFilter(key: String, default: Int = 0): Int =
    (filters[key] as? Int) ?: default
```

## Implementation Strategy

### Option 1: Gradual Migration (Recommended)

1. **Phase 1**: Add `filters` map to `WindowResult` with default empty map
2. **Phase 2**: Add new filter actions while keeping `ChangeFilter`
3. **Phase 3**: Update specific plugins to use new filter system
4. **Phase 4**: Create common filter components

### Option 2: Plugin-Local State (Alternative)

Keep global state minimal, use plugin-local state for complex filters:

```kotlin
// In plugin's present() method
var localFilters by remember { 
    mutableStateOf(mapOf<String, Any>()) 
}

// Sync with global search term
LaunchedEffect(result.searchTerm) {
    // Update local filters when global search changes
}

// Local filter updates don't need global actions
localFilters = localFilters + ("key" to value)
```

## Recommended Approach: Hybrid Solution

### 1. Enhanced WindowResult (Minimal Global State)

```kotlin
data class WindowResult<out T>(
    val searchTerm: String,
    val result: List<T>,
    val filterState: Map<String, Any> = emptyMap() // For persistence only
)
```

### 2. Plugin-Local Filter Management

```kotlin
@Composable
override fun present(result: WindowResult<T>, onAction: OnAction) {
    // Local filter state with persistence
    var filters by remember(result.filterState) { 
        mutableStateOf(
            FilterState(
                systemApps = result.getBooleanFilter(PackagesFilters.SYSTEM_APPS, true),
                userApps = result.getBooleanFilter(PackagesFilters.USER_APPS, true),
                // ... other filters
            )
        )
    }
    
    // Persist filter changes to global state
    LaunchedEffect(filters) {
        onAction(Action.UpdatePluginFilters(id, filters.toMap()))
    }
    
    // Use filters for local filtering logic
    val filteredItems = items.filter { item ->
        applyFilters(item, filters, result.searchTerm)
    }
}
```

### 3. Plugin-Specific Filter State Classes

```kotlin
// Packages Plugin
data class PackagesFilterState(
    val systemApps: Boolean = true,
    val userApps: Boolean = true,
    val enabled: Boolean = true,
    val debuggable: Boolean = false,
    val hasSignature: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        PackagesFilters.SYSTEM_APPS to systemApps,
        PackagesFilters.USER_APPS to userApps,
        PackagesFilters.ENABLED to enabled,
        PackagesFilters.DEBUGGABLE to debuggable,
        PackagesFilters.HAS_SIGNATURE to hasSignature
    )
}
```

## Benefits of Hybrid Approach

### 1. Plugin Autonomy

- Each plugin manages its own filter logic
- No coupling between plugin filter implementations
- Easy to add plugin-specific filter types

### 2. State Persistence

- Filter state survives plugin switches
- Can be saved/restored across app sessions
- Minimal global state pollution

### 3. Performance

- Local filtering doesn't trigger global state updates
- Only persistence updates go through Redux flow
- No unnecessary re-renders across plugins

### 4. Backward Compatibility

- Existing search functionality unchanged
- Gradual migration path
- No breaking changes to existing plugins

## Generic Components

### Enhanced Search Bar

```kotlin
@Composable
fun <F> EnhancedSearchBar(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    filterState: F,
    onFilterChange: (F) -> Unit,
    filterContent: @Composable (F, (F) -> Unit) -> Unit
)
```

### Usage Example

```kotlin
EnhancedSearchBar(
    searchTerm = result.searchTerm,
    onSearchChange = { onAction(Action.ChangeFilter(id, it)) },
    filterState = filters,
    onFilterChange = { filters = it }
) { currentFilters, updateFilters ->
    // Plugin-specific filter checkboxes
    Row {
        FilterCheckbox("System Apps", currentFilters.systemApps) {
            updateFilters(currentFilters.copy(systemApps = it))
        }
        FilterCheckbox("User Apps", currentFilters.userApps) {
            updateFilters(currentFilters.copy(userApps = it))
        }
    }
}
```

This design maintains the plugin pattern while providing a flexible, type-safe filter system that each plugin can
customize to its needs.