# Testing

## Test Types

### Unit Tests

- **Location**: `c3po-core/src/test/kotlin/` and `c3po-desktop/src/test/kotlin/`
- **Purpose**: Test isolated business logic, commands, reducers, middlewares
- **Naming**: `ClassNameTest.kt`
- **Speed**: Fast execution, mocked dependencies
- **Examples**: `AdbCommand.parse()`, reducer state transitions, middleware behavior

### Instrumentation Tests

- **Location**: `c3po-desktop/src/test/kotlin/instrumentation/`
- **Purpose**: Test complete UI workflows, plugin interactions, state management flows
- **Naming**: `FeatureInstrumentationTest.kt`
- **Speed**: Slower execution, real component interaction
- **Framework**: Compose Multiplatform testing + JUnit 5
- **Examples**: Plugin lifecycle, user workflows, cross-plugin navigation, error handling

## Structure

- Tests live in `src/test/kotlin/`
- Use **MockK** for mocking
- Use **AssertK** for assertions
- `Fixtures.kt` for test data
- Use **Compose UI Test** for instrumentation tests

## Coverage

- Use `./gradlew koverHtmlReport`
- UI files are excluded from coverage requirements

## General rules

- Tests should focus the feature, not coverage
- Tests that does not add to the whole purpose, suggest to delete
- For new features, use TDD
- Unit tests for business logic, instrumentation tests for user workflows

## What to do (and not to do)

### Avoid JUnit assertions. Use AssertK instead

**Do this**:

```
assertThat(someString).isEqualTo("correct value")
assertThat(someBoolean).isTrue()
assertThat(someObject).isNotNull()
```

**Don't do this**:

```
assertEquals("correct value", someString)
assertTrue(someBoolean)
assertNotNull(someObject)
```

### Avoid try/catch to test failures

**Do this**

```
assertFailure {
    command.parse(errorOutput)
}.messageContains("Restart failed: Permission denied")
```

**Don't do this**

```
try {
    command.parse(errorOutput)
    assertThat(false).isTrue() // Should not reach here
} catch (e: RuntimeException) {
    assertThat(e.message!!).contains("Restart failed: Permission denied")
}
```

### Avoid multiple assertions over the same object in a test

**Do this**

```
assertThat(result).all {
    prop(AppState::updateState).isEqualTo(UpdateState.Installing)
    prop(AppState::installProgress).isEqualTo("Installing application...")
}
```

```
verify { 
    mockProcessor.reduce(Action.UpdateInstallProgress("Validating installation file...")) 
    mockProcessor.reduce(Action.UpdateInstallProgress("Starting installation process...")) 
}        
```

**Don't do this**

```
assertThat(result.updateState).isEqualTo(UpdateState.Installing)
assertThat(result.installProgress).isEqualTo("Installing application...")
```

```
verify { mockProcessor.reduce(Action.UpdateInstallProgress("Validating installation file...")) }
verify { mockProcessor.reduce(Action.UpdateInstallProgress("Starting installation process...")) }        
```

### Avoid multiple different assertions

**Do this**

```
... // Test 1
assertThat(result.updateState).isEqualTo("Installing application...")
...

... // Test 2
verify { foo.bar() }
...
```

**Don't do this**

```
... // Test 1
assertThat(result.updateState).isEqualTo("Installing application...")
verify { foo.bar() }
...
```

### Avoid testing Enums, Sealed, and Data classes

## Instrumentation Testing Patterns

### Test Setup Structure

```kotlin
@OptIn(ExperimentalTestApi::class)
class PluginInstrumentationTest {
    private lateinit var app: App
    private lateinit var mockExecutor: CommandExecutor

    @BeforeEach
    fun setUp() {
        startKoin {
            modules(testModule {
                single { mockExecutor }
                // ... other test dependencies
            })
        }
        app = App()
    }

    @Test
    fun `should load plugin data when user selects plugin`() = runComposeUiTest {
        // Arrange
        every { mockExecutor.go(any(), any(), any()) } returns
            Result.success("mock_output")

        // Act & Assert
        setContent {
            val state by app.listen().collectAsState()
            PluginUI(state) { action -> app.perform(action) }
        }

        // Trigger plugin selection
        app.perform(Action.StartPlugin("PLUGIN_ID"))

        // Verify loading state appears
        app.listen().test {
            val state = awaitItem()
            assertThat(state.commandStatus).isEqualTo(CommandStatus.Running)
        }

        // Verify data loads
        composeTestRule.onNodeWithText("Expected Content").assertIsDisplayed()
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}
```

### State Verification Patterns

```kotlin
// Wait for specific app state changes
app.listen().test {
    val state = awaitItem()
    assertThat(state).all {
        prop(AppState::commandStatus).isEqualTo(CommandStatus.Completed)
        prop(AppState::windows).key("PLUGIN_ID").isNotNull()
    }
}

// Verify UI reflects state correctly with timeout
composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule.onAllNodesWithText("Loading...").fetchSemanticsNodes().isEmpty()
}
```

### Mock Strategy for ADB Commands

```kotlin
// Mock successful command execution
every { mockExecutor.go(any(), any(), any()) } returns
    Result.success(fixtureData("broadcasts/api30/sample.txt"))

// Mock command failure
every { mockExecutor.go(any(), any(), any()) } returns
    Result.failure(RuntimeException("ADB connection failed"))

// Mock specific command with realistic delay
every { mockExecutor.go(match { it.command.contains("shell") }, any(), any()) } coAnswers {
    delay(100) // Simulate network delay
    Result.success("realistic_output")
}
```

### Plugin Workflow Testing Scenarios

1. **Plugin Loading**: Verify `CommandStatus.Running` → data delivery → `CommandStatus.Completed`
2. **Search/Filter**: Test `ChangeFilter` updates and result filtering
3. **User Actions**: Verify button clicks dispatch correct actions and update state
4. **Error Recovery**: Test error states and user ability to retry/recover
5. **Cross-Plugin Navigation**: Ensure state preservation when switching plugins

### Instrumentation Test Organization

```
c3po-desktop/src/test/kotlin/
├── instrumentation/
│   ├── plugins/
│   │   ├── DevicePluginWorkflowTest.kt
│   │   ├── PackagesPluginWorkflowTest.kt
│   │   └── CrossPluginNavigationTest.kt
│   ├── core/
│   │   ├── AppStateFlowTest.kt
│   │   └── ErrorHandlingFlowTest.kt
│   └── TestFixtures.kt
```

### Best Practices for Instrumentation Tests

- **Test User Journeys**: Focus on complete workflows users perform
- **Mock Realistically**: Use actual ADB output formats from `fixtures/`
- **Test Loading States**: Verify UI feedback during async operations
- **Verify Error Recovery**: Ensure graceful failure handling
- **Keep Tests Isolated**: Each test should start with clean state
- **Test Plugin Integration**: Verify plugins work within app framework
- **Use Semantic Testing**: Test what users see, not implementation details

