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
   git clone https://github.com/yourusername/ThumbWar.git
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

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

## Testing

### Running Tests
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport
```

### Writing Tests
- Place unit tests in `app/src/test/java/`
- Place instrumentation tests in `app/src/androidTest/java/`
- Aim for 70%+ code coverage
- Use descriptive test names: `testX_whenY_thenZ()`
- Mock external dependencies with MockK

Example unit test:
```kotlin
class ThumbWarTest {
    @Test
    fun collisionDetector_detectsOverlap_whenThumbsTouch() {
        val thumb1 = ThumbEntity(1, Vector2(100f, 100f), 30f)
        val thumb2 = ThumbEntity(2, Vector2(120f, 100f), 30f)
        
        assertTrue(CollisionDetector.detectOverlap(thumb1, thumb2))
    }
}
```

## Documentation

### When to Update Documentation
- Adding new features → Update ARCHITECTURE.md
- Changing game mechanics → Update ARCHITECTURE.md
- Adding new screen → Update README.md
- Changing setup process → Update CONTRIBUTING.md

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
   - ✅ Lint checks

### Code Review
- Be responsive to review feedback
- Discuss disagreements respectfully
- Make requested changes in new commits
- Resolve conversation threads once addressed

### Merging
Once approved and all checks pass, your PR will be merged to the main branch.

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

Contributors will be recognized in the project README and commit history.

Thank you for helping make ThumbWar better! 🎮
