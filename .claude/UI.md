# C3PO UI Design System

This document defines the UI standards and guidelines for C3PO to ensure consistency, maintainability, and a cohesive
user experience across all plugins and components.

## Core Design Principles

- **Consistency**: All UI elements should follow established patterns
- **Material3 Adherence**: Leverage Material Design 3 principles throughout
- **Android Green Identity**: Maintain consistent brand theming
- **Accessibility**: Ensure readable, predictable, and accessible interactions
- **Plugin Architecture**: UI patterns that scale across the plugin system

## Theme System

### Color Scheme

Use the `AndroidGreenTheme` exclusively:

```kotlin
AndroidGreenTheme {
    // Your UI content
}
```

### Key Colors

- **Primary**: Android Green (`#218c4a`) for main actions and branding
- **Surface**: White/Dark gray for card backgrounds
- **OnSurface**: Dark text on light surfaces, white text on dark surfaces
- **SurfaceVariant**: Light gray for secondary surfaces and filters
- **Error**: Red (`#D32F2F`) for error states

### Typography

Use Material3 typography consistently:

- `headlineSmall`: Section titles, empty states
- `titleLarge`: Dialog titles
- `titleMedium`: Subsection headers
- `titleSmall`: Item titles, labels
- `bodyMedium`: Regular text content
- `bodySmall`: Secondary text, descriptions
- `labelMedium`: Button text, badges

## Component Usage Guidelines

### Buttons

#### ✅ **Use Standard Button Components**

**Primary Button** - Main actions, CTAs:

```kotlin
PrimaryButton(
    text = "Save Script",
    onClick = { /* action */ },
    enabled = true,
    modifier = Modifier
)
```

**Secondary Button** - Cancel, secondary actions:

```kotlin
SecondaryButton(
    text = "Cancel",
    onClick = { /* action */ },
    enabled = true,
    modifier = Modifier
)
```

#### ❌ **Avoid Raw Material Components**

```kotlin
// DON'T use raw Button components
Button(onClick = {}) { Text("Save") }
OutlinedButton(onClick = {}) { Text("Cancel") }
TextButton(onClick = {}) { Text("OK") }
```

### Action Buttons

**Icon-only Actions**:

```kotlin
CustomActionButton(
    icon = Icons.Filled.Close,
    contentDescription = "Remove item",
    onClick = { /* action */ }
)
```

### Text Input

**All text inputs** should use the custom component:

```kotlin
CustomTextField(
    value = text,
    onValueChange = { newText -> /* update */ },
    placeholder = "Enter text...",
    modifier = Modifier.fillMaxWidth()
)
```

### Dialogs

#### ✅ **Use StandardDialog Pattern**

```kotlin
StandardDialog(
    title = "Confirm Action",
    onDismiss = { /* dismiss */ },
    primaryAction = DialogAction(
        text = "Confirm",
        onClick = { /* primary action */ },
        isPrimary = true
    ),
    secondaryAction = DialogAction(
        text = "Cancel", 
        onClick = { /* secondary action */ }
    )
) {
    // Dialog content
    Text("Are you sure you want to continue?")
}
```

#### ❌ **Avoid Raw AlertDialog**

```kotlin
// DON'T use AlertDialog directly
AlertDialog(
    onDismissRequest = {},
    title = { Text("Title") },
    confirmButton = { TextButton(...) },
    dismissButton = { TextButton(...) }
)
```

### Headers & Structure

**Section Headers**:

```kotlin
EnhancedHeaderRow("Section Title")
```

**Card Layouts** (standard pattern):

```kotlin
Card(
    modifier = Modifier.fillMaxWidth().padding(16.dp),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card content
    }
}
```

## Layout Patterns

### Plugin Structure

All plugins should follow this basic structure:

```kotlin
@Composable
override fun present(result: WindowResult<T>, onAction: OnAction) {
    val state = result.result.firstOrNull() ?: DefaultState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header card with main actions
        Card(/* header card pattern */) {
            Row(/* action buttons */) {
                PrimaryButton(/* primary action */)
                SecondaryButton(/* secondary action */)
            }
        }
        
        // Content area
        if (state.hasContent) {
            ContentCard(state, onAction)
        } else {
            EmptyStateCard()
        }
    }
    
    // Modal dialogs and overlays
    if (state.showDialog) {
        StandardDialog(/* dialog content */)
    }
}
```

### Empty States

Consistent empty state pattern:

```kotlin
Box(
    modifier = Modifier.fillMaxSize().padding(32.dp),
    contentAlignment = Alignment.Center
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No items found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Description of what to do next",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
```

### Spacing Standards

Use consistent spacing throughout:

- **Small spacing**: `8.dp`
- **Medium spacing**: `16.dp`
- **Large spacing**: `32.dp`
- **Card padding**: `16.dp`
- **Card corner radius**: `12.dp`

## Code Examples

### ❌ Before (Non-compliant)

```kotlin
// Raw Material components
Button(onClick = { }) { Text("Save") }
OutlinedButton(onClick = { }) { Text("Cancel") }

// Raw AlertDialog
AlertDialog(
    onDismissRequest = {},
    confirmButton = { TextButton {} },
    dismissButton = { TextButton {} }
)
```

### ✅ After (Compliant)

```kotlin
// Standard components
PrimaryButton(text = "Save", onClick = { })
SecondaryButton(text = "Cancel", onClick = { })

// Standard dialog
StandardDialog(
    title = "Dialog Title",
    primaryAction = DialogAction("Save", { }),
    secondaryAction = DialogAction("Cancel", { })
)
```

## Required Imports

Always include these imports when creating UI components:

```kotlin
// Standard components
import ui.component.PrimaryButton
import ui.component.SecondaryButton
import ui.component.StandardDialog
import ui.component.DialogAction
import ui.component.CustomTextField
import ui.component.EnhancedHeaderRow
import ui.component.CustomActionButton

// Theme
import ui.AndroidGreenTheme
```

## Plugin Review Checklist

Before submitting any UI changes, verify:

- [ ] **Buttons**: Using `PrimaryButton`/`SecondaryButton` instead of raw `Button`/`OutlinedButton`
- [ ] **Dialogs**: Using `StandardDialog` with `DialogAction` pattern
- [ ] **Text Fields**: Using `CustomTextField` for all text inputs
- [ ] **Headers**: Using `EnhancedHeaderRow` for section titles
- [ ] **Theme**: Wrapped in `AndroidGreenTheme` where applicable
- [ ] **Spacing**: Following 8dp/16dp/32dp spacing standards
- [ ] **Imports**: Including all required component imports
- [ ] **Colors**: Using theme colors instead of hardcoded values
- [ ] **Typography**: Using Material3 typography styles

## Common Violations

### Button Issues

❌ `Button()` → ✅ `PrimaryButton()`  
❌ `OutlinedButton()` → ✅ `SecondaryButton()`  
❌ `TextButton()` in dialogs → ✅ `DialogAction` pattern

### Dialog Issues

❌ `AlertDialog` with `TextButton` → ✅ `StandardDialog` with `DialogAction`  
❌ Custom dialog styling → ✅ Standard dialog patterns

### Layout Issues

❌ Hardcoded colors → ✅ Theme colors  
❌ Inconsistent spacing → ✅ 8dp/16dp standards  
❌ Custom text fields → ✅ `CustomTextField`

## Resources

- **Component Definitions**: `c3po-desktop/src/main/kotlin/ui/component/`
- **Theme Definition**: `c3po-desktop/src/main/kotlin/ui/AndroidGreenTheme.kt`
- **Example Plugin**: `c3po-desktop/src/main/kotlin/plugins/device/` (compliant reference)
- **Spacing Constants**: `c3po-desktop/src/main/kotlin/ui/definitions/Dimens.kt`

---

**Remember**: Consistency in UI creates a professional, cohesive user experience. When in doubt, follow existing
patterns from compliant plugins and always use the standard component library.