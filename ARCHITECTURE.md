# ThumbWar Architecture

ThumbWar is a single-activity Android app written in Kotlin with Jetpack Compose. It follows
**MVVM**: a plain-Kotlin game engine (model), a `GameViewModel` that runs the game loop, and
Compose screens that render its state. How the game plays is described in
[GAME_MECHANICS.md](GAME_MECHANICS.md).

## Project Structure

```
app/src/main/java/com/thumbwar/
├── ai/
│   ├── AiController.kt       # Computer opponent; injectable Random for deterministic tests
│   └── AiDifficulty.kt       # EASY, MEDIUM, HARD
├── audio/
│   ├── SoundManager.kt       # ToneGenerator sounds + Vibrator, each switchable
│   └── Sounds.kt             # GameSound enum
├── data/
│   ├── PreferencesRepository.kt  # Sound, vibration, default difficulty (DataStore)
│   └── StatsRepository.kt        # Wins, losses, streaks vs the computer (DataStore)
├── engine/                   # Pure Kotlin, no Android dependencies
│   ├── ArenaScale.kt         # Normalized <-> world units (1.0 = arena's shorter side)
│   ├── CollisionDetector.kt  # Pin detection and who the pinner is
│   ├── GameConfig.kt         # All tunable constants
│   ├── GameEngine.kt         # Advances one tick: movement, pins, rounds, scores
│   ├── GameState.kt          # GamePhase enum, immutable GameState/ThumbState snapshots
│   ├── PhaseManager.kt       # Countdown timing and phase changes
│   └── ThumbEntity.kt        # A thumb's position, velocity, target, pin flags
├── input/
│   ├── InputEvent.kt         # Move(player, position) / Release(player)
│   └── InputManager.kt       # Tracks each finger; assigns it to a player
├── navigation/NavGraph.kt    # Routes: menu, game, settings, game over
├── ui/
│   ├── components/           # ArenaCanvas, ThumbCanvas, PinProgressBar, CountdownOverlay, ScoreDisplay
│   ├── screens/              # game (GameScreen + GameViewModel), gameover, menu, settings
│   └── theme/                # Colors, typography, Material 3 dark theme
├── util/Vector2.kt           # 2D vector math
├── MainActivity.kt
└── ThumbWarApplication.kt
```

## Data Flow

```
Finger touches the arena
  → GameScreen's pointerInput sends every pointer to GameViewModel.processPointer
  → InputManager classifies it (down / held / lifted) and assigns a player
  → InputEvent → GameEngine.setPlayerNTarget / releasePlayerN

Game loop (GameViewModel, a coroutine in viewModelScope, every ~16 ms)
  → AiController picks player 2's target (vs computer only)
  → GameEngine.tick(delta): move thumbs, detect/advance/cancel pins, end rounds
  → sounds and vibration for what changed; stats recorded when a match ends
  → new GameState published on a StateFlow

GameScreen collects the StateFlow → Compose redraws the arena and overlays
```

The engine never reads the clock or touches Android. The ViewModel owns time (an injectable
`clock`), input and side effects, which is what makes the engine easy to test.

## Game Phases

`READY → COUNTDOWN → PLAYING ⇄ PIN_IN_PROGRESS → GAME_OVER`

`GAME_OVER` ends a **round**. `GameState.isMatchOver` says whether it also ends the match:

- Match continues: `GameScreen` shows the round banner, waits 2 s, then calls
  `GameViewModel.startNextRound()`, which goes back to `COUNTDOWN`.
- Match over: `GameScreen` waits 1.5 s, then calls `onGameOver`, and navigation shows the
  game-over screen.

## Key Components

### GameViewModel

- **Dependencies are passed in** (`SoundManager`, the sound/vibration settings as `Flow`s,
  `StatsRepository`, a clock and the AI's `Random`). `GameViewModel.Factory` builds the real ones
  from the `Application`; tests pass fakes.
- **`initialize()` runs once** per match. The screen calls it whenever it's composed, including
  after the activity is recreated (dark mode, font size, split-screen), and later calls are
  ignored so a match in progress isn't reset.
- **Pausing.** `GameScreen` forwards `ON_PAUSE`/`ON_RESUME`. While paused, the loop is stopped,
  held fingers are released, and a round that starts in the meantime waits for `resume()`.

### GameEngine and CollisionDetector

`GameEngine.tick` moves both thumbs, then either checks for a new pin (`PLAYING`) or advances or
cancels the current one (`PIN_IN_PROGRESS`). All distances go through `ArenaScale`, so a pin
starts when the thumbs touch on screen at any aspect ratio. The pinner is the thumb moving
faster toward the other.

### InputManager

A Compose pointer event lists every finger on the screen, not just the one that changed, so each
pointer is classified by its own `pressed`/`previousPressed` flags. In 2-player mode the side a
finger first touches picks its player for as long as it stays down.

### Persistence

`PreferencesRepository` and `StatsRepository` wrap Preferences DataStore. Each takes a
`DataStore<Preferences>` (tests use a temporary file) and has a `Context` constructor for the app.

### Rendering

The arena and thumbs are drawn on a Compose `Canvas` (`ArenaCanvas`, `ThumbCanvas`) from
normalized positions; the pinning thumb is drawn on top and a pinned thumb is squished with X
eyes. Menus and overlays are regular Material 3 composables. Screens scroll when content doesn't
fit, such as a phone in landscape.

## Testing

| Where | What | Runs |
|---|---|---|
| `app/src/test` | Engine, AI, input, repositories, ViewModel (virtual time) | JVM, CI |
| `app/src/test` (Robolectric) | Whole-app flows through `MainActivity`, `GameScreen` played with touch input, navigation, pixel checks of the canvas, `SoundManager` | JVM, CI |
| `app/src/androidTest` | Compose UI tests of screens | On a device (`./gradlew connectedDebugAndroidTest`) |

Coverage is measured with JaCoCo (`./gradlew jacocoTestReport`) and uploaded to Codecov from CI.
Tests that involve randomness use a seeded `Random`; tests that involve time use a fake clock.

## Build, CI and Release

- `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`.
- **Release** (`./gradlew assembleRelease`) is shrunk with R8 and signed with `release.keystore`
  using credentials from `local.properties`; both are git-ignored and exist only locally. Every
  update must be signed with the same key, so keep a backup of both.
- **CI** (GitHub Actions, `.github/workflows/ci-cd.yml`): ktlint, detekt, debug build, unit tests,
  coverage upload to Codecov (authenticated with GitHub OIDC, no stored token), Android Lint,
  and a Trivy config scan.

## Future Improvements

1. **Multiplayer**: Network play with other devices
2. **Leaderboards**: Cloud-based high score tracking
3. **Themes**: Multiple visual themes
4. **Sound Effects**: Custom audio files and music
5. **Analytics**: User engagement tracking
6. **Monetization**: In-app purchases for cosmetics
