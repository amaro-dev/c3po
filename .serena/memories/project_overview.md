# C3PO Project Overview

## Purpose

C3PO is a desktop Android debugging and exploration tool built with Kotlin and Jetpack Compose Desktop. It connects to
Android devices via ADB and provides a plugin-based architecture for exploring apps, activities, services, permissions,
and more.

## Key Features

- Plugin-based architecture for extensibility
- Redux-style state management using Sonic library
- Material Design 3 UI with custom Android Green theme
- Multi-module structure (core, desktop, Android Studio plugin)
- Socket communication with companion Android service
- Comprehensive Android debugging capabilities

## Target Users

- Android developers needing device debugging tools
- Security researchers analyzing Android applications
- Quality assurance engineers testing Android apps
- Anyone needing deep Android device exploration capabilities

## Project Goals

- Provide comprehensive Android device debugging through ADB
- Extensible plugin system for adding new features
- Modern desktop UI using Jetpack Compose
- Cross-platform compatibility (currently focused on macOS)
- Integration with Android Studio as a plugin