# Testing

## Structure

- Tests live in `src/test/kotlin/`
- Use **MockK** for mocking
- Use **AssertK** for assertions
- `Fixtures.kt` for test data

## Coverage

- Use `./gradlew koverHtmlReport`
- UI files are excluded from coverage requirements

## General rules

- Tests should focus the feature, not coverage
- Tests that does not add to the whole purpose, suggest to delete
- For new features, use TDD

## What to do (and not to do)

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

