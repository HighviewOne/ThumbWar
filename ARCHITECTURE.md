# ThumbWar Architecture

## Overview

ThumbWar is an Android thumb wrestling game built with modern Android technologies. This document describes the architecture, design patterns, and key components.

## Architecture Pattern: MVVM + MVI

The project uses a hybrid of **Model-View-ViewModel (MVVM)** and **Model-View-Intent (MVI)** patterns for state management and UI rendering.

- **Model Layer**: Game engine, repositories, and data sources
- **ViewModel Layer**: `GameViewModel` managing game state and user intents
- **View Layer**: Jetpack Compose UI components

## Project Structure

```
app/src/main/java/com/thumbwar/
├── ai/                          # AI difficulty and controller logic
│   ├── AiController.kt         # AI decision-making for single-player mode
│   └── AiDifficulty.kt         # Enum: EASY, MEDIUM, HARD
│
├── audio/                       # Sound & vibration handling
│   └── SoundManager.kt         # System sounds and haptic feedback
│
├── data/                        # Data layer (repositories)
│   ├── StatsRepository.kt      # Handles win/loss/streak statistics
│   └── PreferencesRepository.kt # User settings (sound, vibration, AI difficulty)
│
├── engine/                      # Core game logic
│   ├── GameEngine.kt           # Main game loop (~60fps)
│   ├── GameConfig.kt           # Game constants (thumb size, pin duration, etc.)
│   ├── GameState.kt            # Data class for current game state
│   ├── GamePhase.kt            # Enum: READY, COUNTDOWN, PLAYING, PIN_IN_PROGRESS, ROUND_OVER, MATCH_OVER
│   ├── PhaseManager.kt         # Manages phase transitions and timing
│   ├── ThumbEntity.kt          # Thumb object with position/velocity/state
│   └── CollisionDetector.kt    # Pin detection and collision logic
│
├── input/                       # Touch input handling
│   ├── InputManager.kt         # Processes touch events and updates thumb positions
│   └── InputEvent.kt           # Data class for touch event data
│
├── navigation/                  # Jetpack Navigation setup
│   └── NavGraph.kt             # Compose NavHost with route definitions
│
├── ui/
│   ├── components/             # Reusable Compose components
│   │   ├── ArenaCanvas.kt      # Game arena rendering with Canvas
│   │   ├── ThumbCanvas.kt      # Thumb sprites and rendering
│   │   ├── PinProgressBar.kt   # Pin progress ring visualization
│   │   ├── CountdownOverlay.kt # Countdown animation ("1, 2, 3, 4, I declare")
│   │   └── ScoreDisplay.kt     # Score and UI overlay
│   │
│   ├── screens/                # Screen-level Compose components
│   │   ├── game/               # Game screen and ViewModel
│   │   ├── gameover/           # Game over screen with results
│   │   ├── menu/               # Main menu screen
│   │   └── settings/           # Settings screen
│   │
│   └── theme/                  # Material3 theme configuration
│
├── util/                        # Utility functions
│   ├── Vector2.kt              # 2D vector math utilities
│   └── Extensions.kt           # Kotlin extension functions
│
├── MainActivity.kt             # Activity entry point
└── ThumbWarApplication.kt      # Application class
```

## Data Flow

### Game State Flow

```
User Input (Touch)
    ↓
InputManager processes touch events
    ↓
InputEvent emitted
    ↓
GameViewModel receives input
    ↓
GameEngine updates game state
    ↓
Thumb positions updated
    ↓
CollisionDetector checks for pin
    ↓
GameState emitted via StateFlow
    ↓
Compose recomposes UI
    ↓
Canvas renders thumbs and arena
```

### Game Phase Flow

```
READY (waiting to start)
    ↓
COUNTDOWN (1, 2, 3, 4, I declare)
    ↓
PLAYING (both thumbs moving)
    ↓
PIN_IN_PROGRESS (thumbs overlapped, counting down)
    ↓
ROUND_OVER (winner determined, showing result)
    ↓
(if best-of-3) → Next round or MATCH_OVER
```

## Key Components

### GameEngine

The heart of the application. Runs a ~60fps game loop that:
- Updates thumb positions based on player input and AI logic
- Detects collisions and pin conditions
- Manages game phase transitions
- Updates game state

```kotlin
// Main game loop runs in a Coroutine
while (isRunning) {
    updateThumbPositions()
    detectCollisions()
    checkPinCondition()
    updatePhase()
    delay(16ms) // ~60fps
}
```

### CollisionDetector

Determines when thumbs are "pinned":
1. Calculate distance between thumb centers
2. Check if distance < sum of radii (overlap)
3. If overlapping, start pin countdown
4. After 2500ms of sustained overlap, pin is successful

### PhaseManager

Manages state machine transitions and timing:
- READY: Initial state, waiting for user to start
- COUNTDOWN: 4-second countdown before game starts
- PLAYING: Active gameplay
- PIN_IN_PROGRESS: Thumbs overlapped, counting down
- ROUND_OVER: Round complete, show winner
- MATCH_OVER: Match complete (for best-of-3)

### InputManager

Handles Android touch events:
- Tracks touch down, move, and up events
- Converts coordinates to game space
- Provides input to GameViewModel
- Supports multi-touch for 2-player mode

### SoundManager

Manages audio and haptics:
- Uses ToneGenerator for system sounds (no audio files)
- Uses Vibrator API for haptic feedback
- Respects user sound/vibration preferences

### Repositories

**StatsRepository**: Tracks and persists game statistics
- Win count, loss count, win streak
- Persisted via DataStore Preferences

**PreferencesRepository**: User settings
- Sound enabled/disabled
- Vibration enabled/disabled
- AI difficulty level
- Persisted via DataStore Preferences

## State Management

Uses Kotlin **StateFlow** for reactive state management:

```kotlin
// GameViewModel exposes state as StateFlow
private val _gameState = MutableStateFlow(initialGameState)
val gameState: StateFlow<GameState> = _gameState.asStateFlow()

// UI collects state and recomposes
val state by viewModel.gameState.collectAsState()
```

Benefits:
- Lifecycle-aware (automatically stops collecting when UI not visible)
- Hot stream (always has current value)
- Thread-safe
- Coroutine-based (works well with Jetpack Compose)

## UI Layer: Jetpack Compose

All UI built with **Jetpack Compose** (no XML layouts):
- Declarative UI composable functions
- Material3 design system
- Custom Canvas-based game rendering for performance
- Responsive layout for landscape orientation

### Game Rendering

Game arena and thumbs rendered using Canvas API for performance:
```kotlin
Canvas(modifier = Modifier.fillMaxSize()) {
    // Draw arena background
    drawRect(Color.Green, size = size)
    
    // Draw thumbs with positions from state
    drawThumb(thumb1Position, thumb1Color)
    drawThumb(thumb2Position, thumb2Color)
    
    // Draw pin progress indicator
    if (pinInProgress) {
        drawPinProgressRing(pinProgress)
    }
}
```

## Testing Strategy

### Unit Tests
- **Engine Logic**: CollisionDetection, GamePhase transitions, AI behavior
- **Utilities**: Vector2 math, input processing
- **Repositories**: Stats persistence, preference handling
- Target: 70%+ code coverage

### Instrumentation Tests
- **UI Components**: Canvas rendering, button interactions
- **Navigation**: Screen transitions and navigation flow
- **End-to-End**: Full game flow from menu to game over

### Test Tools
- **JUnit 4**: Unit test framework
- **Compose Testing**: Compose-specific UI testing
- **MockK**: Mocking for dependencies
- **Jacoco**: Code coverage reporting
- **Coroutines Test**: Coroutine testing utilities

## Performance Considerations

1. **Game Loop**: Optimized for ~60fps using Coroutines
2. **Canvas Rendering**: Direct Canvas drawing instead of composables for better performance
3. **State Updates**: Only relevant composables recompose when state changes
4. **Memory**: Efficient data structures and minimal allocations in game loop

## Accessibility Features

- Content descriptions for UI elements
- Minimum touch target sizes (48dp)
- High contrast colors for visibility
- Keyboard navigation support where applicable

## Security

- **Keystore Credentials**: Stored in local.properties (git-ignored), not hardcoded
- **Data Persistence**: Uses DataStore for secure preference storage
- **Input Validation**: Touch coordinates validated against arena boundaries
- **ProGuard Obfuscation**: Enabled for release builds

## Dependencies

### Core Android
- androidx.core:core-ktx
- androidx.lifecycle:lifecycle-runtime-ktx
- androidx.activity:activity-compose

### Compose
- androidx.compose.ui:ui
- androidx.compose.material3:material3
- androidx.compose.foundation:foundation
- androidx.navigation:navigation-compose

### State & Data
- androidx.datastore:datastore-preferences
- org.jetbrains.kotlinx:kotlinx-coroutines-android

### Testing
- junit:junit
- androidx.test.ext:junit
- androidx.compose.ui:ui-test-junit4
- io.mockk:mockk

## Build & Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

Release build includes:
- Code obfuscation (ProGuard)
- Minification (shrinking)
- Signing with keystore credentials from local.properties

### CI/CD

GitHub Actions workflow runs:
- Code quality checks (Detekt, ktlint, Android Lint)
- Unit tests
- Code coverage reporting
- Debug APK builds
- Security scanning

## Future Improvements

1. **Multiplayer**: Network play with other devices
2. **Leaderboards**: Cloud-based high score tracking
3. **Themes**: Multiple visual themes
4. **Sound Effects**: Custom audio files and music
5. **Analytics**: User engagement tracking
6. **Monetization**: In-app purchases for cosmetics
