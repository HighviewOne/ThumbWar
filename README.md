# Thumb War

A digital thumb wrestling game for Android. Control cartoon thumbs on a touchscreen and try to pin your opponent's thumb down.

**[Download APK](https://github.com/HighviewOne/ThumbWar/releases/latest/download/app-debug.apk)**

Requires Android 8.0 or higher. Enable "Install from unknown sources" in device settings to install.

## Features

- **VS Computer** — 3 AI difficulties (Easy, Medium, Hard)
- **2 Players** — same device, landscape mode, one thumb each
- **Countdown** — "1, 2, 3, 4, I declare a thumb war!"
- **Best of 3** — choose single round or best-of-3 match before each game
- **Pin mechanics** — overlap your opponent and hold for 2.5 seconds to win
- **Sound & haptics** — audio feedback and vibration on game events
- **Stats tracking** — wins, losses, and win streaks persisted across sessions
- **Settings** — toggle sound/vibration, set default difficulty

## How to Play

1. Touch the screen to control your thumb
2. Move your thumb onto your opponent's thumb
3. Keep it pinned — a progress ring fills from yellow to red
4. If the ring completes, you win!
5. The pinned player can escape by dragging away

## Documentation

- **[Architecture Overview](ARCHITECTURE.md)** — Project structure, design patterns, and component descriptions
- **[Game Mechanics](GAME_MECHANICS.md)** — Detailed explanation of game rules, physics, and AI behavior
- **[Contributing Guide](CONTRIBUTING.md)** — How to contribute, development setup, and code style
- **[Accessibility Features](ACCESSIBILITY.md)** — Accessibility support and usage guidelines
- **[API Documentation](https://github.com/HighviewOne/ThumbWar/wiki)** — Generated KDoc and API reference

## Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34 recommended)
- Git

### Setup & Build

```bash
git clone https://github.com/HighviewOne/ThumbWar.git
cd ThumbWar

# Copy the local.properties template
cp local.properties.template local.properties

# For debug builds, just sync Gradle (no keystore needed)
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

For detailed development setup, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Code Quality

This project maintains high code quality standards:

- ✅ **Detekt** — Kotlin linting and static analysis
- ✅ **ktlint** — Code formatting and style checks
- ✅ **Unit Tests** — 70%+ code coverage
- ✅ **Instrumentation Tests** — UI and integration testing
- ✅ **ProGuard** — Code obfuscation for release builds
- ✅ **CI/CD** — Automated testing and building via GitHub Actions

Run checks locally:
```bash
./gradlew detekt ktlintCheck testDebugUnitTest jacocoTestReport
```

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material3)
- **State Management**: Coroutines + StateFlow
- **Data Persistence**: DataStore Preferences
- **Rendering**: Canvas API (procedural, no bitmap assets)
- **Game Loop**: Coroutine-based (~60fps)
- **Testing**: JUnit 4, MockK, Compose Testing, Espresso
- **Build System**: Gradle 8.5 with Kotlin DSL

## Project Structure

```
app/src/main/java/com/thumbwar/
├── engine/          — Game loop, collision detection, phases
├── ui/              — Jetpack Compose screens and components
├── input/           — Touch input handling
├── ai/              — AI opponent logic
├── audio/           — Sound and vibration
├── data/            — Repositories and persistence
└── util/            — Math and utilities
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed breakdown.

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Development setup
- Code style guidelines
- Testing requirements
- Pull request process

## License

[MIT License](LICENSE) — Free to use and modify

## Acknowledgments

Built with ❤️ using modern Android best practices and technologies.

