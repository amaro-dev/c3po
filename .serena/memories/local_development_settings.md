# Local Development Settings

## Java Configuration

**JAVA_HOME**: `/Library/Java/JavaVirtualMachines/jdk-19.jdk/Contents/Home`

### Available Java Versions

- `adoptopenjdk-11.jdk` - Too old for this project
- `jdk-19.jdk` - **Currently used for builds**
- `microsoft-11.jdk` - Too old for this project

### Build Commands

- **Build entire project**:
  `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-19.jdk/Contents/Home && ./gradlew build`
- **Build desktop module only**:
  `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-19.jdk/Contents/Home && ./gradlew :c3po-desktop:compileKotlin`
- **Run application**: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-19.jdk/Contents/Home && ./gradlew run`

## Common Issues

- The c3po-plugin module has some Java version compatibility warnings but doesn't affect desktop builds
- Some deprecation warnings are expected and don't affect functionality
- Build may show warnings but should complete successfully

## Development Rules

1. **Always clean unused classes, functions and properties** after refactoring
2. **Save local settings** to avoid rechecking environment setup
3. **Use existing components** when possible to maintain consistency
4. **Test compilation** after major changes using the desktop module build