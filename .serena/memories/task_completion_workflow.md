# Task Completion Workflow for C3PO

## When a Task is Completed

### 1. Code Quality Checks

```bash
# Run all tests to ensure nothing is broken
./gradlew test

# Generate and check test coverage
./gradlew koverHtmlReport
# Review coverage report at: build/reports/kover/html/index.html
```

### 2. Build Verification

```bash
# Clean build to ensure everything compiles correctly
./gradlew clean build

# Test the application runs correctly
./gradlew run
```

### 3. Code Style Verification

- Ensure file organization follows the established patterns
- Verify each class/component is in its own file
- Check package declarations match directory structure
- Confirm Material Design color usage (no hardcoded colors)

### 4. Plugin-Specific Checks (if applicable)

- Verify plugin is registered in `di/AppModule.kt` PLUGIN_LIST_DEPENDENCY
- Ensure plugin actions are properly defined and handled
- Test plugin UI integration in the sidebar
- Verify plugin state management works correctly

### 5. Architecture Compliance

- Confirm Redux/MVI patterns are followed
- Verify middleware handles actions appropriately
- Check that commands extend proper base classes
- Ensure dependency injection is configured correctly

### 6. Documentation Updates

- Update CLAUDE.md if architectural changes were made
- Add any new commands to the common commands section
- Document any new patterns or conventions introduced

## Pre-Commit Checklist

- [ ] All tests pass (`./gradlew test`)
- [ ] Coverage requirements met (`./gradlew koverHtmlReport`)
- [ ] Clean build succeeds (`./gradlew clean build`)
- [ ] Application runs without errors (`./gradlew run`)
- [ ] Code follows established conventions
- [ ] New plugins are properly registered
- [ ] Documentation is updated if needed

## Debugging Failed Tasks

- **Build failures**: Check Java 17+ requirement and Gradle setup
- **UI rendering issues**: Verify Material3 imports vs Material2, check color usage
- **State not updating**: Verify action dispatching and middleware implementation
- **ADB connection problems**: Check device authorization and ADB path configuration
- **Plugin not showing**: Verify plugin registration in AppModule.kt