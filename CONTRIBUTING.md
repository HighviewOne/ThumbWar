# Contributing to ThumbWar

Thank you for your interest in contributing to ThumbWar! This guide will help you get started.

## Development Setup

### Prerequisites
- Android Studio (latest version)
- JDK 17 or higher
- Android SDK (API 34 recommended)
- Git

### Local Setup
1. Clone the repository
   ```bash
   git clone https://github.com/HighviewOne/ThumbWar.git
   cd ThumbWar
   ```

2. Create `local.properties` with your keystore credentials
   ```bash
   cp local.properties.template local.properties
   ```
   Edit `local.properties` and add your keystore credentials (or leave empty for debug builds)

3. Open in Android Studio and let Gradle sync

4. Build and run on an emulator or device
   ```bash
   ./gradlew installDebug
   ```

## Code Style & Quality

We enforce code quality through automated checks. All contributions must pass:

### Running Quality Checks Locally
```bash
# Format code with ktlint
./gradlew ktlintFormat

# Check code with Detekt
./gradlew detekt

# Run all checks
./gradlew build
```

### Code Style Guidelines
- Use Kotlin conventions (PascalCase for classes, camelCase for variables)
- Maximum line length: 120 characters
- Use meaningful variable names
- Add KDoc comments to public APIs
- Follow the project structure - don't create new top-level packages

### Before Committing
1. Run `./gradlew ktlintFormat` to auto-format
2. Run `./gradlew detekt` and fix any issues
3. Run `./gradlew testDebugUnitTest` to run tests
4. Ensure your changes don't break existing tests

## Making Changes

### Creating a Feature Branch
```bash
git checkout -b feature/your-feature-name
# or for bug fixes
git checkout -b fix/your-bug-fix
```

### Writing Code
1. Follow the existing code patterns in the codebase
2. Keep changes focused and atomic
3. Add unit tests for new logic
4. Update documentation if needed

### Commit Messages
Use clear, descriptive commit messages:
```
Short summary (50 chars max)

More detailed explanation if needed, wrapped at 72 characters.
Explain *what* changed and *why*, not *how*.
```

Prefixes used in this repo: `fix:`, `feat:`, `test:`, `docs:`, `ci:`, `build:`, `chore:`.

## Testing

### Running Tests
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumentation tests (requires a device or emulator)
./gradlew connectedDebugAndroidTest

# Generate coverage report (app/build/reports/jacoco/jacocoTestReport/html)
./gradlew jacocoTestReport
```

Instrumentation tests install a debug build, which is signed with a different key than the
releases. If a release build of Thumb War is on the device, uninstall it first.

### Writing Tests
- **Unit tests** go in `app/src/test/java/` and run on the JVM in CI. This includes UI tests:
  Compose screens and Android APIs run under **Robolectric** (see `AppFlowTest`,
  `GameScreenTest`, `CanvasRenderingTest`), so prefer them over device tests.
- **Instrumentation tests** go in `app/src/androidTest/java/` for things that need real hardware.
- **Keep tests deterministic.** Pass a seeded `Random` to `AiController`/`GameViewModel`, and a
  fake clock to `GameViewModel` (`GameViewModelTest` runs the game loop in virtual time;
  `GameScreenTest` shows how to drive the loop and Compose's clock together under Robolectric).
- Name tests by behavior, e.g. `` `pinned player escapes by dragging away` ``.
- Mock external dependencies with MockK; repositories take a `DataStore`, so tests can use a
  real one backed by a temp file (see `RepositoriesTest`).
- Keep coverage where it is (about 90% on Codecov); the badge in the README tracks it.

Example unit test:
```kotlin
class CollisionDetectorTest {
    @Test
    fun `pin detected when thumbs overlap`() {
        val thumb1 = ThumbEntity(0.5f, 0.5f)
        val thumb2 = ThumbEntity(0.5f + GameConfig.THUMB_RADIUS, 0.5f)
        thumb1.setTarget(Vector2(0.8f, 0.5f))
        thumb1.update(0.016f) // moving toward thumb2 makes thumb1 the pinner

        val result = CollisionDetector().checkPin(thumb1, thumb2)
        assertTrue(result.isPinning)
        assertEquals(1, result.pinnerPlayer)
    }
}
```

## Documentation

### When to Update Documentation
- Changing structure or components → Update ARCHITECTURE.md
- Changing rules, constants or AI behavior → Update GAME_MECHANICS.md
- Adding a feature or screen → Update README.md
- Changing accessibility → Update ACCESSIBILITY.md
- Changing setup, testing or release steps → Update CONTRIBUTING.md

### Documentation Standards
- Use clear, concise language
- Include code examples where helpful
- Keep documentation in sync with code
- Update README.md for user-facing changes

## Submitting Changes

### Creating a Pull Request
1. Push your branch to your fork
   ```bash
   git push origin feature/your-feature-name
   ```

2. Create a pull request on GitHub
   - Give it a clear title
   - Reference any related issues
   - Describe what changed and why

3. Ensure all CI/CD checks pass:
   - ✅ Code quality (Detekt, ktlint)
   - ✅ Unit tests
   - ✅ Code coverage
   - ✅ Android Lint (errors fail the build; warnings are reported only)

### Code Review
- Be responsive to review feedback
- Discuss disagreements respectfully
- Make requested changes in new commits
- Resolve conversation threads once addressed

### Merging
Once approved and all checks pass, your PR will be merged to the main branch.

## Releasing (maintainers)

Release APKs are built and signed locally; CI doesn't build releases.

1. Make sure `main` has a user-facing change since the last release and CI is green.
2. Bump `versionCode` (+1) and `versionName` in `app/build.gradle.kts`.
3. `./gradlew clean ktlintCheck detekt testDebugUnitTest assembleRelease`. Signing uses
   `release.keystore` and the passwords in `local.properties`, both git-ignored.
4. Check the APK is signed with the release key, the same one as every earlier release, or
   existing installs can't update:
   `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`
5. Install it over the previous release on a device (`adb install -r`) and smoke-test it.
6. Commit the bump (`chore: bump to vX.Y.Z`), tag `vX.Y.Z`, and push both.
7. `gh release create vX.Y.Z app/build/outputs/apk/release/app-release.apk --title vX.Y.Z`
   with player-facing notes. Keep the asset named **`app-release.apk`**: the README and website
   Download buttons link to `releases/latest/download/app-release.apk`.

Keep a backup of `release.keystore` and its passwords somewhere safe. If they're lost, no
future release can install over existing copies.

## Project Structure

Key directories to know:
- `app/src/main/java/com/thumbwar/` - Main source code
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumentation tests
- `app/src/main/res/` - Resources
- `.github/workflows/` - CI/CD configuration

## Reporting Issues

Found a bug? Please report it!

1. Check existing issues first
2. Create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Device and Android version info
   - Screenshots if applicable

## Feature Requests

Have an idea? We'd love to hear it!

1. Check existing issues/discussions
2. Open a discussion or issue describing:
   - What you want to add
   - Why it would be useful
   - Any implementation ideas

## Getting Help

- **Documentation**: Read ARCHITECTURE.md for code organization
- **Issues**: Check closed issues for solutions
- **Discussions**: Start a discussion for general questions

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

## Recognition

Contributors are credited in the commit history and release notes.

Thank you for helping make ThumbWar better! 🎮
