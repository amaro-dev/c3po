# C3PO Executive Project – Light Theme

## Visual Inspiration and Implementation Guidance

> **Visual Reference:**
> The UI should be implemented to match the level of polish, clarity, and modern design seen in the provided sample images (see design references attached to this document).
> - Use rounded corners, card-based layouts, and clear visual hierarchy.
> - Apply vibrant accent colors, soft backgrounds, and modern iconography.
> - Ensure all components (sidebar, top bar, cards, tables, dialogs, etc.) follow the spacing, elevation, and color cues from the images.
> - Prioritize clarity, accessibility, and a friendly, approachable look.

**Instruction to the agent:**
When implementing the UI, always refer to the sample images for visual style, layout, and interaction cues. All design system and component specifications in this document should be interpreted and executed to achieve a result visually consistent with those references.

## 1 Introduction & Scope

C3PO is a Kotlin Compose Desktop application that helps developers inspect and control Android devices. It exposes internal ADB commands through a friendly UI and allows tasks such as listing installed packages, starting activities or services, viewing declared permissions, inspecting device attributes, viewing pending intents, examining APK signatures and building automation scripts. This executive project describes the design and functional specification for C3PO with a unified look and feel, ensuring consistent behaviour across the application. It documents navigation, the design system (colors, typography, icons and spacing), reusable UI components, detailed screen specifications, global rules (navigation, feedback, error handling), accessibility, non‑functional requirements and the dark‑theme appendix.

### 1.1 How to use it

The AI agent must follow these steps and rules to implement this execution plan in the App

1. Read this entire document
2. Understand it and correlate with the current code
3. Make a sequential plan to implement in the following order
  3.1. Initial screen and its flows
  3.2. Activities tab
  3.3. Packages tab
  3.4. Attributes tab
  3.5. Services tab
  3.6. Permissions tab
  3.7. Signature tab
  3.8. Automation tab

PS: For now we are droping the Pending Intents tab feature

### 1.2 Rules to follow

This app uses MVI architecture which heavily improves the possibility to deacouple the UI from business logic. 

You should perform the implementation without making any changes to the business logic at first. This should be considered at the beggining of your plan.

By doing this, you will be in a better position to define what are the necessary changes in logic. 

The MVI architecture allows for sepearated flow/logic and UI state. We should also consider that this architecture tries to make components independent up to certain level. We should stick to this approach too

## 2 Navigation Architecture

C3PO uses a **shell + plugin** navigation pattern. On the left, a permanent **sidebar** lists all available plugins: *Packages, Permissions, Activities, Services, Pending Intents, Device Attributes, Signature, Automation*. Clicking an entry sends `Action.StartPlugin(pluginId)` and causes the main content area to switch to the selected screen. The sidebar highlights the current selection and provides hover and focus feedback. A **top bar** above the content area contains:

- **Device selector**: a drop–down listing connected devices; selecting a device triggers `Action.SelectDevice(device)`.
- **Refresh devices** button: triggers `Action.RefreshDevices`.
- **Companion status** indicator and button: opens the companion installation dialog.
- **Global feedback** area: shows running status or error overlays.

The **main content area** displays the currently selected plugin. Each plugin provides its own UI within this area and is responsible for dispatching actions back to the middlewares. Global overlays (settings box, feedback overlay, companion dialog) can temporarily cover the content area when necessary.

## 3 Design System (Light Theme)

### 3.1 Color palette (light theme)

The light theme uses a fresh Android–inspired green palette with neutral backgrounds:

| Role           | Hex value | Use case                                   |
|----------------|-----------|---------------------------------------------|
| Primary        | `#3DDC84` | Main accents: buttons, active icons, links |
| Primary Light  | `#E6F7F0` | Tints for hover or selected backgrounds     |
| Secondary      | `#2EB872` | Secondary accents, chips                    |
| Background     | `#F9FAFB` | Page background                             |
| Surface        | `#FFFFFF` | Cards, panels, lists                       |
| Text Primary   | `#212121` | High‑contrast text                         |
| Text Secondary | `#616161` | Secondary labels                           |
| Success        | `#2EB872` | Positive badges, success messages          |
| Error          | `#D32F2F` | Error badges, failure messages             |

These colors follow Material 3 guidelines to ensure accessible contrast by pairing foreground and background roles appropriately [oai_citation:0‡developer.android.com](https://developer.android.com/develop/ui/compose/designsystems/material3#:~:text=Dynamic%20color%20is%20designed%20to,color%20scheme%20accessible%20by%20default). Avoid pairing tertiary variants with primary surfaces, as this reduces contrast [oai_citation:1‡developer.android.com](https://developer.android.com/develop/ui/compose/designsystems/material3#:~:text=Dynamic%20color%20is%20designed%20to,color%20scheme%20accessible%20by%20default). Where dynamic theming is used, choose tonal values that maintain contrast for light surfaces.

### 3.2 Typography

Use **Inter** or **Poppins** for all text. Headings use larger weights (SemiBold or Bold) while body text uses Regular or Medium weights. Adopt a simplified type scale that scales across devices; the Material 3 type scale provides categories such as Display, Headline, Title, Body and Label that adjust their sizes depending on context [oai_citation:2‡developer.android.com](https://developer.android.com/develop/ui/compose/designsystems/material3#:~:text=The%20M3%20type%20scale%20updates,categories%20that%20scale%20across%20devices). For example, the page title uses the “Title Large” style, the section headings use “Title Medium”, table headers use “Label Large” and body text uses “Body Medium”. Ensure a minimum 1.5× line height for readability.

### 3.3 Icons

Icons come from the **Lucide** thin‑line set. They have a consistent stroke width (1.5 px) and use the primary color or text color depending on context. Icons appear before labels in buttons and lists when they add meaning (e.g., a trash icon for *Uninstall*). Provide accessible names for icons for screen readers.

### 3.4 Spacing & Layout

Follow an 8 px baseline grid. Use 16 px margins at the edges of panels and between major sections, and 8 px padding within cards and list rows. Buttons have a minimum touch height of 36 px, and lists use comfortable row heights (48–56 px). Card corners use 8 px radius, and list rows have 4 px radius on hover. Shadow elevations differentiate surfaces: surfaces like panels use a small shadow, while modals use a larger shadow.

## 4 UI Component Reference

This section defines reusable components and their properties. Properties are descriptive (no code) to guide the development using Kotlin Compose.

### 4.1 Buttons

**Purpose**: Initiate an action (primary), alternative action (secondary) or text‑only link.

**Properties**:
- *Size*: Minimum width 88 px; height 36–40 px; horizontal padding 16 px; rounded corners 8 px.
- *Colors*: 
  - Primary button uses primary color for background and on‑primary for text/icons.
  - Secondary button uses a transparent background with a 1 px primary border; text in primary color.
  - Text button uses no border and transparent background; text in primary color.
- *States*: 
  - **Default**: normal background and text colors.
  - **Hover**: background tinted with primary light; border darkens slightly.
  - **Pressed**: background darkens; text remains on‑primary.
  - **Disabled**: background neutral (e.g., `#E0E0E0`); text `#9E9E9E`; no pointer events.
- *Behavior*: 
  - Buttons support an optional left icon; maintain 8 px spacing between icon and label.
  - Click events trigger the associated action.
- *Accessibility*: Provide an accessible label describing the action; ensure focus outline visible.

### 4.2 Search Field

**Purpose**: Filter lists by user input.

**Properties**:
- *Size*: Full width of its parent container; height 40 px; 16 px left/right padding.
- *Elements*: Text input, optional search icon, clear text button when not empty.
- *Colors*: Background `#F0F0F0`, border `#DDDDDD`, text `#212121`, placeholder text `#9E9E9E`.
- *States*:
  - **Focused**: border changes to primary color; show caret.
  - **Hover**: subtle border highlight.
  - **Error**: border changes to error color with accessible error message below.
- *Behavior*: 
  - Debounce input; filtering triggers after 300 ms.
  - Provide placeholder text like “Search packages…”.
- *Accessibility*: Input should have a `labelFor` or accessible name; ensure clear button has an accessible description (“Clear search text”).

### 4.3 Data Table & List Rows

**Purpose**: Present a collection of items, optionally grouped.

**Properties**:
- *Structure*: Table header (optional) followed by rows; grouping headers separate groups.
- *Row layout*: Each row uses a horizontal layout with leading icon/avatar area (24 px), main content (left aligned), optional actions (buttons) right aligned. Rows have 48–56 px height and 8 px horizontal padding.
- *Colors*: Even/odd rows can alternate slight background tint for readability; actions use primary or secondary colors.
- *States*: 
  - **Hover**: row background tinted with primary light; pointer indicates row is interactive.
  - **Selected** (if applicable): row background tinted with secondary color; maintain text contrast.
  - **Disabled**: row appears dim; pointer disabled.
- *Behavior*: 
  - Clicking row (when supported) triggers default action; action buttons handle specific tasks.
  - Expandable rows (e.g., signature card) reveal additional details below the row; expansion indicator rotates.
- *Accessibility*: Rows should be focusable; group headers should be announced; action buttons need accessible labels.

### 4.4 Card

**Purpose**: Contain related information (e.g., device selection cards, step list items).

**Properties**:
- *Size*: Minimum width 300 px; flexible height; internal padding 16 px.
- *Colors*: Surface color for background; slight elevation shadow; border radius 8 px.
- *Content*: Title (bold, medium size), subtitle (secondary text), actions aligned at bottom or right.
- *States*: 
  - **Hover**: elevation increases slightly; border color darkens.
  - **Pressed**: shadow compresses; background darkens.
  - **Disabled**: reduces opacity.
- *Behavior*: Card acts as a button when selecting an item (e.g., device), otherwise static.

### 4.5 Group Header / Section Title

**Purpose**: Label groups within lists (e.g., package names grouping activities) or separate sections.

**Properties**:
- *Text style*: Label Large (uppercased, 500 weight).
- *Spacing*: 16 px top margin before header; 8 px bottom margin.
- *Decorative line*: subtle 1 px divider below header.

### 4.6 Badges & Chips

**Purpose**: Display compact status or filters.

**Properties**:
- *Size*: Height 24 px; horizontal padding 8–12 px.
- *Colors*:
  - Success badge: background success color; text on‑success color (`#FFFFFF`).
  - Error badge: background error color; text on‑error (`#FFFFFF`).
  - Neutral chip: border `#DDDDDD`; background `#F5F5F5`; text `#616161`.
- *States*:
  - **Interactive chip**: toggles between selected (filled with primary color) and unselected (outlined).
  - **Badge**: static; no interaction.

### 4.7 Dialogs & Modals

**Purpose**: Prompt user for confirmation or additional input (e.g., step configuration, settings).

**Properties**:
- *Size*: Width 400–600 px depending on content; vertical padding 24 px; horizontally centred.
- *Structure*: Title at top; content body; action bar with buttons aligned right.
- *Colors*: Surface background; overlay scrim with 40 % opacity; content text in primary color; destructive actions use error color.
- *Behavior*: 
  - Dismiss via Escape key or Cancel button.
  - Trap focus inside dialog until closed.
- *Accessibility*: Use appropriate roles; provide focus management; ensure keyboard navigation.

### 4.8 File Drop Zone

**Purpose**: Accept a file (e.g., APK) via drag‑and‑drop or file picker.

**Properties**:
- *Appearance*: Dotted or solid border (2 px) with rounded corners; icon (e.g., upload arrow); prompt text.
- *Size*: Minimum 300 px wide by 200 px tall.
- *States*: 
  - **Idle**: neutral border and background.
  - **Drag‑over**: border changes to primary color; background tinted; instructive text emphasised.
  - **Error**: border changes to error color; error message displayed.
- *Behavior*: 
  - Accepts only .apk files; file picker triggered on click.
  - After file drop, collapse and show progress or result.

### 4.9 Toast / Alert

**Purpose**: Display transient feedback (success, warning, error).

**Properties**:
- *Placement*: Floating at bottom right of window; auto dismiss after a few seconds.
- *Colors*: 
  - Success: success background; white text.
  - Error: error background; white text.
  - Info: primary background; white text.
- *Icon*: Prepend appropriate icon (check, alert, error).
- *Behavior*: 
  - Appear with fade/slide animation.
  - Provide close button with accessible label.

### 4.10 Step List & Step Configuration Dialogs (Automation)

**Purpose**: Manage automation steps.

**Properties**:
- *Step list item*: Card with step title (e.g., “Install APK”), optional summary (e.g., file name), reorder drag handle and edit/remove icons. Height ~60 px; background surface color; border radius 8 px.
- *States*: 
  - **Selected**: tinted with primary light; highlight reorder handle.
  - **Disabled**: greyed out when run is in progress.
- *Configuration dialogs*: Use generic dialog style; include fields relevant to step type (text inputs, drop‑downs or file pickers).

## 5 Screen Specifications

The following sections detail each plugin screen. The functional information derives from the `c3po-screens-map.md` file extracted from the codebase and the UI design described above.

### 5.1 Main Window (Framework)

**Purpose**: Provide the application shell with navigation and device selection.

**Components**: 
- Sidebar with plugin list (buttons with icons).
- Top bar with device selector, refresh button, companion status indicator.
- Content area to host plugin UI.
- Global feedback overlay (running indicator and error alert).
- Settings overlay (for ADB path).
- Companion dialog (installation prompt).

**Behaviour**:
- Selecting a plugin updates the content area via `Action.StartPlugin(pluginId)`.
- Device selection triggers `Action.SelectDevice(device)`.
- Refresh button triggers `Action.RefreshDevices`.
- Show `SettingsBox` overlay when settings are missing; overlay prevents interaction with underlying UI.
- Show `CompanionDialog` overlay when companion installation is required; user choices dispatch install or skip actions.

**States**:
- **No device**: Content area displays placeholder text prompting the user to select or connect a device.
- **Settings missing**: Settings overlay visible; main content disabled until a valid ADB path is saved.
- **Running command**: Feedback overlay shows spinner and description of current command.
- **Error**: Feedback overlay shows error message and dismiss button.

**Data displayed**: List of connected `AdbDevice`s; current plugin id; global window map containing each plugin’s result.

**Acceptance criteria**:
- When the application launches with no settings configured, the settings overlay appears and user can save the ADB path.
- When a device is connected and selected, plugin screens display data accordingly.
- Error overlay appears whenever middleware dispatches an error; dismissing the overlay resets the error state.

### 5.2 Installed Packages

**Purpose**: Display all installed packages on the selected device and provide actions to manage them.

**Components**:
- Search field to filter by package name (requires at least 3 characters).
- Data table with rows representing `AppPackage`s. Columns: package name; version name with version code in parentheses.
- Action buttons per row: 
  - Sleep/Awake indicator: toggles retrieval of the sleep state; icon reflects Unknown, Sleeping, Awake.
  - Extract Key: shows cryptographic signature information.
  - Uninstall: removes the package from the device.
  - Stop: stops the app process.
  - Clear Data: clears the app’s user data.
- Optional expansion panel: When signature info is available, a row can expand to reveal `SignatureCard` with certificate details.
- Divider lines between rows.

**Behaviour**:
- Typing in the search field filters the list; search triggers after 3 characters.
- Clicking Sleep/Awake indicator dispatches `Actions.CheckAsleep(pkg)` if state unknown; the cell updates once the state is received.
- Clicking Extract Key dispatches `Actions.ExtractKey(pkg)`; on success the row expands to show the signature details.
- Clicking Uninstall dispatches `Actions.Uninstall(pkg)` and shows a confirmation dialog; after success the row disappears.
- Clicking Stop dispatches `Actions.Stop(pkg)`; row shows a spinner while command executes.
- Clicking Clear Data dispatches `Actions.ClearData(pkg)`; confirm before clearing.
- All actions show toasts upon success or error.

**States**:
- **Loading**: Show spinner in content area while `ListPackagesCommand` executes.
- **Empty**: Show “No packages found” if the list is empty or the filter yields no results.
- **Success**: Display table with package rows.
- **Error**: Show feedback overlay with error details.

**Data displayed**:
- Package name (string).
- Version name and code (string/int).
- Sleep state: Unknown, Asleep, Awake.
- Signature info: list of certificate entries (if available).

**Preconditions**: A device must be connected and selected.

**Acceptance criteria**:
- Filtering shows only packages whose names contain the typed substring (case insensitive).
- Action buttons dispatch the correct commands and update the UI accordingly.
- Errors in any command display appropriate feedback.

### 5.3 Declared Permissions

**Purpose**: Show Android manifest permissions per application and allow copying the permission key.

**Components**:
- Search field to filter by permission name (requires at least 3 characters).
- Owner header rows: package names that own the permissions.
- Permission rows: permission key and stamps (flags) indicating permission level (e.g., `dangerous`, `normal`).
- Copy button per permission row.

**Behaviour**:
- Typing in the search field filters the permission list.
- Clicking the copy button dispatches `Action.CopyText(permissionKey)` and shows a toast.

**States**:
- **Loading**: while `ListDeclaredPermissions` runs.
- **Empty**: no permissions or filter mismatch.
- **Success**: list displayed.
- **Error**: error overlay.

**Data displayed**:
- `DeclaredPermissions(ownerApp: String, permissions: Map<String, List<PermissionFlag>>)`.

**Preconditions**: Device must be selected.

### 5.4 Activities

**Purpose**: List activities grouped by package and allow launching them.

**Components**:
- Search field (min 3 characters).
- Group headers for package names.
- Rows: show activity fully qualified path; actions: “Start” and “Start for Debug”.

**Behaviour**:
- Typing filters by package or full path.
- Clicking “Start” dispatches `ActivitiesPlugin.Actions.Launch(activityInfo, forDebug=false)`.
- Clicking “Start for Debug” dispatches `ActivitiesPlugin.Actions.Launch(activityInfo, forDebug=true)`.

**States**:
- **Loading**: while `ListActivitiesCommand` executes.
- **Empty**: no activities found or filter mismatch.
- **Error**: error overlay.
- **Success**: list displayed.

**Data displayed**:
- `ActivityInfo` containing `packageName` and `activityPath`.
- Activities grouped by `packageName`.

**Preconditions**: Device selected.

### 5.5 Services / Actions

**Purpose**: Show services or actions grouped by package and allow launching them.

**Components**:
- Search field (min 3 characters).
- Group headers by package.
- Rows: show `activityPath`; single “Start” action.

**Behaviour**:
- Typing filters.
- Clicking “Start” dispatches `ServicesPlugin.Actions.Launch(ActivityInfo)`.

**States**: Same as Activities.

**Data displayed**:
- List of pairs `(packageName, List<ActivityInfo>)`.

**Preconditions**: Device selected.

### 5.6 Pending Intents

**Purpose**: Display pending intents grouped by package (read‑only list).

**Components**:
- Group header per package.
- Rows rendered via `PendingIntentRow` (contains the intent description).

**Behaviour**: None (no actions).

**States**:
- **Loading**: while `ListPendingActivityIntentsCommand` runs.
- **Empty**, **Error**, **Success** similar to previous lists.

**Data displayed**:
- List of pairs `(packageName, List<PendingIntent>)`.

**Preconditions**: Device selected.

### 5.7 Device Attributes

**Purpose**: Show key/value pairs describing device properties and allow copying values.

**Components**:
- Search field (filter by key or value; min 2 characters).
- Rows: show key on left, value on right; copy button to copy entire line or value.

**Behaviour**:
- Typing filters the list.
- Clicking copy dispatches `Action.CopyText(value)` and shows toast.

**States**:
- **Loading**: while `DeviceInfoCommand` runs.
- **Empty**, **Error**, **Success** similar to other lists.

**Data displayed**:
- List of pairs `(key: String, value: String)`.

**Preconditions**: Device selected.

### 5.8 APK Signature

**Purpose**: Let the user select or drop an APK file and display its signing certificate report.

**Components**:
- Centered file drop zone (`FileBox`).
- After a file is loaded, a report view (`Report`) showing certificate details.

**Behaviour**:
- Drag‑and‑drop or click to select an `.apk` file.
- On drop, dispatch `SignaturePlugin.Actions.LoadFile(filePath)`; show loading indicator while processing.
- On success, display the report with sections for certificate fingerprint, validity and other metadata.
- On error (invalid file or parse failure), show error message and allow the user to try again.

**States**:
- **Initial**: drop zone visible; instructions shown.
- **Loading**: progress indicator (spinner or linear progress).
- **Success**: report displayed; option to select another file.
- **Error**: error message displayed; drop zone remains available.

**Data displayed**:
- `AndroidPackageReport`: certificate fingerprint(s), signer name, algorithm, validity dates, etc.

**Preconditions**: None (works offline).

### 5.9 Automation

**Purpose**: Create, edit, save and run automation scripts composed of sequential steps.

**Components**:
- Header bar: buttons to create a new script and open an existing script.
- Script creation panel: input field for script name; list of steps with reorder and remove icons; “Add Step” drop‑down.
- Step configuration dialogs:
  - PackageSelectorDialog: search installed packages and select one.
  - ActivitySelectorDialog: search activities to select.
  - ApkPickerDialog: file picker for APKs.
- Run logs card: displays tail of last 50 log entries; scrollable.
- Script folder picker: system file chooser for selecting script directory.

**Behaviour**:
- Create New Script: dispatch `AutomationPlugin.Actions.CreateNewScript` and reset the editor.
- Open Script: dispatch `AutomationPlugin.Actions.OpenScript` to open a folder picker; read `script.c3po` and load its contents; show `openScriptError` if invalid.
- Editing:
  - Set script name via `Actions.SetScriptName(name)`.
  - Add a step using the drop‑down; selects type (`InstallApk`, `RemovePackage`, `StartActivity`, `ClearData`).
  - Remove a step via remove icon; confirm deletion.
  - Reorder steps via drag handle.
  - Edit a step: open corresponding configuration dialog; dispatch configuration actions upon save.
- Save Script: dispatch `Actions.SaveScript`; validate script; save YAML into `scripts/{name}/script.c3po`; show success or error.
- Run Script: dispatch `Actions.RunScript`; set `isRunning` flag; sequentially execute steps via `ScriptRunner` and update `runningStepIndex`; logs update in real time.
- Cancel Script: dispatch `Actions.CancelScript`.

**States**:
- **Creating** vs. **Browsing** (script not loaded).
- Dialog visibility flags: show/hide configuration dialogs (package/activity/apk pickers, script folder picker).
- Running: `isRunning`, `runningStepIndex`, `runLogs`.
- Errors: `openScriptError`, `malformedScriptFolderPath`.

**Data displayed**:
- `AutomationState`: script metadata (name, version, format version), list of steps with configured parameters.
- Preloaded packages and activities for pickers.
- Log entries with timestamp and message.

**Preconditions**:
- For running: Device selected; script saved; resources (APK) available in script folder.
- For open: selected folder contains valid YAML (`script.c3po`).

**Acceptance criteria**:
- Users can create a new script, add steps, edit them, reorder and remove; save the script; run and cancel.
- Running script executes steps sequentially; logs update; errors stop execution with message.

## 6 Global Rules

- **Navigation consistency**: All plugin screens should use the same layout: search at top (if applicable), list or content area below, actions aligned right; consistent spacing.
- **Error handling**: Any command error triggers a global error overlay with the error message and a dismiss button; plugin screens should not display stack traces. This overlay should be accessible and maintain focus until dismissed.
- **Loading indicators**: Use spinners or progress bars to communicate background operations; ensure they have accessible labels.
- **Empty states**: Provide friendly messages and icons when lists are empty; suggestions to connect device or adjust filters.
- **Keyboard navigation**: All interactive elements must be reachable via Tab; show focus indicator; pressing Enter or Space should trigger primary actions.
- **Responsiveness**: The layout must adapt to different window sizes; lists should scroll; side navigation collapses to icons-only when window width is limited.
- **Consistency across plugins**: Common actions such as copying text, refreshing lists and launching items should behave the same and show consistent feedback.

## 7 Accessibility & Internationalization

C3PO must meet accessibility standards. Key guidelines:

- **Color contrast**: Use tonal palettes and role pairings to achieve accessible contrast; avoid pairing tertiary container colors with primary surfaces [oai_citation:3‡developer.android.com](https://developer.android.com/develop/ui/compose/designsystems/material3#:~:text=Dynamic%20color%20is%20designed%20to,color%20scheme%20accessible%20by%20default). Ensure text contrast ratio meets WCAG AA (4.5:1 for body text).
- **Dynamic type**: Use a dynamic type scale so that typography scales with window size and device context [oai_citation:4‡developer.android.com](https://developer.android.com/develop/ui/compose/designsystems/material3#:~:text=The%20M3%20type%20scale%20updates,categories%20that%20scale%20across%20devices). Allow users to adjust font sizes via system settings.
- **Semantic roles**: Provide accessible names for all buttons, icons and input fields; group related controls; use proper headings.
- **Keyboard accessibility**: All interactive elements must be reachable and operable via keyboard; manage focus within dialogs; provide focus outlines.
- **Screen reader support**: Use content descriptions; avoid relying solely on color or icons to convey meaning.
- **Internationalization**: Text strings must be externalized for translation; follow local formatting for dates, times and numbers; support right‑to‑left languages by mirroring layouts.

## 8 Non‑Functional Requirements (NFRs)

- **Performance**: UI must remain responsive while executing ADB commands; use coroutines and background threads; show progress indicators for long operations.
- **Resource constraints**: Manage memory consumption by limiting list sizes (e.g., lazy lists); release resources after use.
- **Offline capabilities**: APK Signature screen should work without a connected device; automation scripts can be edited offline but require device for running.
- **Security**: Validate inputs (e.g., file extensions); handle command outputs safely; sanitize logs; restrict file operations to user directories.
- **Scalability**: Architecture should allow adding new plugins easily; design tokens and components should be extensible.
- **Reliability**: Provide clear error messages and recovery instructions for ADB issues (device offline, unauthorized).
- **Telemetry**: Optionally instrument key actions (e.g., plugin open, command run) to improve the tool; follow privacy guidelines.

## 9 Dark Theme Appendix

The dark theme uses the same structure and components but adjusts colors for dark surfaces. Key changes:

| Role           | Light Theme      | Dark Theme         | Notes                                               |
|----------------|------------------|--------------------|-----------------------------------------------------|
| Background     | `#F9FAFB`        | `#121212`          | Overall window background                           |
| Surface        | `#FFFFFF`        | `#1E1E1E`          | Cards and panels                                    |
| Primary        | `#3DDC84`        | `#3DDC84`          | Primary color remains; maintain sufficient contrast |
| Primary Light  | `#E6F7F0`        | `#294B38`          | Tint used for hover/selected states                 |
| Secondary      | `#2EB872`        | `#2EB872`          | Secondary accent; lighten slightly if needed        |
| Text Primary   | `#212121`        | `#EDEDED`          | Light text on dark surfaces                         |
| Text Secondary | `#616161`        | `#A8A8A8`          | Secondary text                                      |
| Success        | `#2EB872`        | `#2EB872`          | Unchanged                                           |
| Error          | `#D32F2F`        | `#EF5350`          | Slightly lighter red for dark background            |

- Shadows are subtler on dark surfaces; use less elevation to avoid high contrast.
- Hover states lighten the surface slightly rather than darken.
- Maintain the same typography and layout.
- Ensure icons and text maintain at least 4.5:1 contrast ratio.

## 10 Implementation Notes

This document is intended to guide an AI or human developer implementing C3PO in Kotlin Compose Desktop:

- **Design tokens**: Centralize colors, typography and spacing in a theme object. Use descriptive names (e.g., `primaryColor`, `surfaceColor`) to allow dark‑theme substitution.
- **Composable functions**: Each UI component described here should map to a `@Composable` function (e.g., `PrimaryButton`, `SearchField`, `DataTableRow`). These functions accept parameters for content and callbacks.
- **State management**: Use Compose state and flows to manage plugin data; recomposition should occur when state changes.
- **Event dispatching**: UI events should dispatch the appropriate `Action` objects to the middleware layer; actions correspond to those listed in each screen specification.
- **Accessibility**: Use `Modifier.semantics` to attach descriptions and roles; ensure `Modifier.focusable` is used for interactive items; provide test tags for automated tests.
- **Navigation**: Maintain the selected plugin state in a `ViewModel` and render the corresponding Composable.
- **Resource loading**: Offload heavy operations (ADB commands, file parsing) to background threads; update UI state via state flows.
- **Error handling**: Wrap command calls in try/catch; send errors to a centralized error state to show the global feedback overlay.

By adhering to this executive project, developers will produce an application that looks polished, behaves consistently and follows accessibility and design guidelines.