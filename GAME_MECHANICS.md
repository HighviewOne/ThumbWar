# ThumbWar Game Mechanics

## Game Overview

ThumbWar is a digital thumb wrestling simulation for Android. Players control cartoon thumbs on a touchscreen to compete in a pin-based game mechanic.

## Core Mechanics

### The Pin

The winning condition is to "pin" your opponent's thumb. A successful pin requires:

1. **Overlap**: The two thumbs must overlap (touch/intersect)
2. **Duration**: The overlap must be maintained for 2.5 seconds
3. **Victory**: After 2.5 seconds of sustained overlap, the pin is successful

#### Pin Detection Algorithm

```
Frame Update Loop:
  1. Calculate distance between thumb centers
  2. If distance < (thumb1.radius + thumb2.radius):
       - Thumbs are overlapping
       - Start/continue pin countdown
  3. If pin countdown >= 2500ms:
       - Pin successful → Current player wins round
       - Award point and transition to ROUND_OVER phase
  4. If overlap is broken:
       - Reset pin countdown
```

#### Collision Detection

Uses circle-to-circle collision (both thumbs are circles):

```
distance = sqrt((x2 - x1)² + (y2 - y1)²)
isColliding = distance < (radius1 + radius2)

Game Constants:
  - Thumb radius: 40 display units
  - Arena width: 1440 px (landscape)
  - Arena height: 810 px (landscape)
```

### Movement

Each thumb has position and velocity:

```kotlin
data class ThumbEntity(
    val id: Int,
    val position: Vector2,        // Current position
    val velocity: Vector2,        // Current velocity (pixels/frame)
    val isPlayerThumb: Boolean,   // Player or AI controlled
    val radius: Float = 40f
)
```

#### Player Thumb
- Moves directly to touch coordinates
- Responds immediately to player input
- Movement is limited to arena boundaries

#### AI Thumb
- Moves autonomously based on AI difficulty
- Uses decision-making algorithm to move toward/away from player thumb
- Adapts strategy based on game state

### Game Phases

The game progresses through distinct phases:

#### READY Phase
- Initial state when game starts
- Neither thumb is visible yet
- Waiting for phase transition to begin

#### COUNTDOWN Phase (4 seconds)
- "1, 2, 3, 4, I declare... THUMB WAR!" countdown
- Thumbs start at opposite ends of arena
- Visual countdown overlay shown to player

#### PLAYING Phase
- Main gameplay
- Player controls their thumb via touch
- AI thumb moves autonomously
- Both thumbs visible and interactive
- Win condition: Pin opponent for 2.5 seconds

#### PIN_IN_PROGRESS Phase
- Triggered when thumbs overlap
- 2.5 second countdown timer displayed
- Progress ring animation shows pin progress
- If countdown completes: transition to ROUND_OVER (current player wins)
- If separation occurs: return to PLAYING phase

#### ROUND_OVER Phase
- Round complete, winner determined
- Show winner announcement
- Display round result (1-0, 0-1, or 1-1)
- Wait for player to continue (tap to start next round or match over)

#### MATCH_OVER Phase (Best-of-3)
- In best-of-3 mode: happens after 2 rounds
- Winner has won 2+ rounds
- Display match results and overall winner
- Show final statistics

### Scoring

#### Single Round
- First player to achieve a successful pin wins the round
- Score: 1 point to winner, 0 to loser

#### Best-of-3 Match
- Play up to 3 rounds
- First player to win 2 rounds wins the match
- Typical scenarios:
  - Player 1: 2-0 (match over after 2 rounds)
  - Player 1: 2-1 (match over after 3 rounds)
  - Player 1: 1-2 (player 1 doesn't win)

### Game Modes

#### VS Computer (AI)
- Single player vs AI opponent
- Three difficulty levels:
  - **Easy**: AI moves slowly and cautiously
  - **Medium**: AI balances offense/defense
  - **Hard**: AI moves aggressively and strategically
- Best-of-3 match option available

#### 2-Player Local
- Two players on same device
- Player 1: Left thumb
- Player 2: Right thumb
- Both players use touch controls simultaneously

### AI Difficulty

AI behavior controlled by `AiDifficulty` enum:

#### Easy
- Slower movement speed
- Less aggressive positioning
- Easier to evade and pin
- Good for learning game mechanics

#### Medium
- Balanced movement speed
- Switches between aggressive and defensive
- Competitive but fair challenge

#### Hard
- Fast movement speed
- Aggressive positioning and pinning strategy
- Difficult to pin
- Challenging opponent for skilled players

## Game Loop

The game runs at approximately **60 frames per second**:

```
While Game Running:
  1. Process Input
     - Read touch events
     - Update player thumb position
  
  2. Update Game State
     - Update AI thumb position (based on difficulty)
     - Apply velocity/movement constraints
     - Ensure thumbs stay within arena
  
  3. Collision Detection
     - Check if thumbs overlap
     - Update pin state and countdown
  
  4. Phase Management
     - Check if phase transition needed
     - Update timers and phase-specific logic
  
  5. Render
     - Draw arena background
     - Draw thumbs at current positions
     - Draw UI overlays (score, timer, progress)
  
  6. Delay
     - Sleep ~16ms to maintain 60fps
```

## Game Constants

Located in `GameConfig.kt`:

```kotlin
object GameConfig {
    const val THUMB_RADIUS = 40f
    const val PIN_DURATION = 2500L  // milliseconds
    const val COUNTDOWN_DURATION = 4000L  // milliseconds
    const val ARENA_WIDTH = 1440f
    const val ARENA_HEIGHT = 810f
    const val DEFAULT_THUMB_SPEED = 5f  // pixels per frame
    const val MAX_THUMB_SPEED = 12f
    const val MIN_BOUNDARY_OFFSET = 50f
}
```

## User Experience Flow

```
1. Main Menu Screen
   ├─ "VS Computer" → AI Difficulty Selection
   │  ├─ Easy   ─┐
   │  ├─ Medium─┼─ Game Starts
   │  └─ Hard  ─┘
   ├─ "2 Players" → Game Starts
   └─ "Settings" → Settings Screen

2. Game Screen
   ├─ Countdown (4s) → "1, 2, 3, 4, I declare"
   ├─ Playing
   │  ├─ Player pins opponent → Pin animation
   │  └─ Round Over
   ├─ Best-of-3? → Next Round or Match Over
   └─ Match Over → Results Screen

3. Game Over Screen
   ├─ Winner announcement
   ├─ Final score
   ├─ Statistics
   └─ Menu buttons (Replay, Menu, Settings)
```

## Settings

Players can customize:

1. **Sound**: Toggle sound effects on/off
2. **Vibration**: Toggle haptic feedback on/off
3. **AI Difficulty**: Select AI opponent difficulty (for VS Computer)

Settings are persisted via DataStore Preferences.

## Statistics Tracking

The game tracks and persists:

- **Total Wins**: Cumulative wins across all games
- **Total Losses**: Cumulative losses across all games
- **Current Streak**: Current winning streak
- **Best Streak**: Best winning streak achieved
- **Win Ratio**: Calculated as wins / (wins + losses)

Statistics are stored per game mode (VS Computer, 2-Player).

## Performance Considerations

### Frame Rate
- Target: 60 FPS
- Implemented using Coroutines with 16ms frame timing
- Canvas rendering for optimal performance

### Touch Latency
- Input processed every frame
- Direct canvas rendering (no expensive recompositions)
- Minimal input-to-display latency

### Memory
- Efficient game state objects
- No per-frame allocations in game loop
- Canvas reused across frames

## Accessibility Features

- **Content Descriptions**: All UI elements have descriptive labels
- **Touch Targets**: Minimum 48dp for all interactive elements
- **Color Contrast**: High contrast for readability
- **Screen Reader Support**: Compatible with TalkBack

## Planned Mechanics (Future)

- Power-ups during gameplay
- Multiple arena environments
- Combo multipliers for consecutive pins
- Special move animations
- Pressure mechanics (hand stamina)
