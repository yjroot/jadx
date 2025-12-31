# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 공통 규칙

- 모든 문서는 한글로 작성합니다.
- 코드나 문서 변경 후 사용자가 별도 지시를 하지 않아도 즉시 커밋하고 푸시합니다.

## Project Overview

JADX is a Dex to Java decompiler - command line and GUI tools for producing Java source code from Android Dex and APK files. Written in Java 11+ with Gradle (Kotlin DSL) build system.

## Build Commands

```bash
# Full build with tests
./gradlew clean build

# Build distribution zip (outputs to build/jadx-<version>.zip)
./gradlew dist

# Run tests
./gradlew test
./gradlew :jadx-core:test              # Core module only
./gradlew :jadx-core:test --tests "jadx.tests.integration.TestClassName"  # Single test class

# Code formatting (required before commits)
./gradlew spotlessCheck                # Check formatting
./gradlew spotlessApply                # Auto-fix formatting

# Run locally
./gradlew :jadx-cli:installDist && build/jadx-cli/bin/jadx [options] <input>
./gradlew :jadx-gui:installDist && build/jadx-gui/bin/jadx-gui

# Check for dependency updates
./gradlew dependencyUpdates
```

## Module Structure

```
jadx-core/          # Core decompilation engine - main logic
jadx-cli/           # Command-line interface (JCommander)
jadx-gui/           # Desktop GUI (Swing + FlatLaf + RxJava)
jadx-commons/
  jadx-app-commons/ # Cross-platform config/cache utilities
  jadx-zip/         # Secure ZIP reader
jadx-plugins/
  jadx-input-api/   # Base API for input plugins
  jadx-dex-input/   # Android DEX bytecode loading
  jadx-java-input/  # Java bytecode support
  jadx-smali-input/ # Smali assembly support
  jadx-java-convert/# Java to DEX conversion
  jadx-rename-mappings/  # Obfuscation mapping support
  jadx-kotlin-metadata/  # Kotlin language support
  jadx-script/      # Kotlin scripting framework
  jadx-*-input/     # Various package format plugins (AAB, XAPK, APKM, etc.)
jadx-plugins-tools/ # Plugin management utilities
```

## Architecture

### Core Decompilation Pipeline

1. **Input Loading** - Plugins load bytecode (DEX, Java, Smali)
2. **Parsing** - Bytecode converted to internal IR (Intermediate Representation)
3. **Analysis** - Type inference, data flow analysis, SSA transformation
4. **Transformation** - Visitor passes optimize and restructure code
5. **Code Generation** - IR converted to Java source

### Key Classes

- `JadxDecompiler` (jadx.api) - Main API entry point
- `RootNode` - Root of the decompiled code tree
- `ClassNode`, `MethodNode`, `FieldNode` - AST components
- `IDexTreeVisitor` - Visitor interface for AST traversal and transformation
- `JadxArgs` - Configuration options

### Visitor Pattern

The core uses extensive visitor pattern via `IDexTreeVisitor`. Decompilation passes are implemented as visitors that traverse and transform the AST. Visitors are applied sequentially - order matters.

Key visitor packages:
- `jadx.core.dex.visitors.*` - Main transformation visitors
- `jadx.core.dex.visitors.ssa.*` - SSA form analysis
- `jadx.core.dex.visitors.typeinference.*` - Type system
- `jadx.core.dex.regions.*` - Control flow analysis

### Plugin System

Plugins implement `JadxPlugin` interface. Main plugin types:
- **Input plugins** - Load various bytecode formats (implement `ICodeLoader`)
- **Pass plugins** - Add transformation passes
- **GUI plugins** - UI extensions

Plugin management via `JadxPluginManager` with event-based communication.

## Code Style

- Java formatting: Eclipse formatter (config in `config/code-formatter/`)
- Kotlin formatting: ktlint with tabs
- Line endings: Unix (LF)
- Encoding: UTF-8
- Run `./gradlew spotlessApply` before committing

## Contributing Requirements

- Use only Java 11 or below features and APIs
- Run full build before submitting: `./gradlew clean build dist`
- No pure style/dependency update PRs
- For decompilation issues, attach APK file (rename to .apk.zip for GitHub)

## Environment Variables

- `JADX_VERSION` - Override version string
- `JADX_BUILD_JAVA_VERSION` - Build with specific Java version
- `JADX_TEST_JAVA_VERSION` - Run tests with specific Java version
- `JADX_DISABLE_XML_SECURITY` - Disable XML security checks
- `JADX_DISABLE_ZIP_SECURITY` - Disable ZIP security checks
