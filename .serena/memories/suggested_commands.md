# C3PO Development Commands

## Building and Running

```bash
# Build the entire project
./gradlew build

# Run the desktop application
./gradlew run

# Clean build (when having issues)
./gradlew clean build

# Package macOS DMG
./gradlew packageDmg
```

## Testing Commands

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew koverHtmlReport
# Coverage report available at: build/reports/kover/html/index.html

# Run specific test class
./gradlew test --tests "ClassName"

# Run tests in specific package
./gradlew test --tests "package.*"

# Run module-specific tests
./gradlew :c3po-desktop:test
./gradlew :c3po-core:test
```

## Module-Specific Commands

```bash
# Build core module only
./gradlew :c3po-core:build

# Build desktop module only
./gradlew :c3po-desktop:build

# Build Android Studio plugin only
./gradlew :c3po-plugin:build
```

## Development Workflow

1. Make code changes
2. Run `./gradlew test` to ensure tests pass
3. Run `./gradlew koverHtmlReport` to check coverage
4. Test the application with `./gradlew run`
5. For releases, build DMG with `./gradlew packageDmg`

## Darwin-Specific Commands

```bash
# List files and directories
ls -la

# Find files
find . -name "*.kt" -type f

# Search in files (using ripgrep if available, otherwise grep)
rg "pattern" --type kotlin
grep -r "pattern" --include="*.kt"

# Git operations
git status
git add .
git commit -m "message"
git push
```