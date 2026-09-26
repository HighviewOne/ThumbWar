# ThumbWar Game Mechanics

How the game plays, with the numbers the code actually uses. Tunable values live in
`app/src/main/java/com/thumbwar/engine/GameConfig.kt`.

## Units

Positions are stored as fractions of the arena (0..1 on each axis). Distances and speeds are
measured in **world units, where 1.0 is the arena's shorter side** (`ArenaScale`). This keeps
collisions and movement matching what's drawn, whatever the screen's aspect ratio. Thumbs are
drawn with the same scale, so what you see is what collides.

| Value | Setting |
|---|---|
| Thumb radius | 0.06 of the shorter side |
| Thumb top speed | 0.8 shorter-sides per second |
| Arena bounds | thumbs stay within 5%–95% of each axis |
| Start positions | P1 at (0.3, 0.5), P2 at (0.7, 0.5) |
| Game tick | ~16 ms (~60 per second) |

## Movement

- A thumb moves toward the point your finger is on, at up to top speed. It doesn't teleport.
- Lift your finger and the thumb glides to a stop.
- The computer's thumb has the same top speed as yours. Difficulty changes how it decides,
  not how fast it moves.

## The Pin

1. **Contact.** When the thumbs come within 2 radii of each other, a pin starts.
2. **Who pins.** The thumb moving faster *toward* the other is the pinner (by a small margin).
   If that's a tie, the faster-moving thumb wins; if both are still, nothing happens.
3. **Holding it.** The pinned thumb can still move, at half speed (`PINNED_SPEED_FACTOR`). The
   pinner has to stay on it. The progress ring fills over **2.5 seconds**.
4. **Escaping.** If the thumbs get **2.5 radii** apart, the pin breaks and progress resets to
   zero. Escaping takes more distance than starting a pin, so small wobbles don't break it.
5. **Winning.** When the ring fills, the pinner wins the round.

## Game Phases

`GamePhase` has five values:

| Phase | What happens |
|---|---|
| `READY` | Before the countdown starts |
| `COUNTDOWN` | "1", "2", "3", "4" (0.8 s each), then "I declare a thumb war!" (1.5 s): 4.7 s in all. Touches are ignored. |
| `PLAYING` | Both thumbs move; contact starts a pin |
| `PIN_IN_PROGRESS` | Ring filling; escape returns to `PLAYING` |
| `GAME_OVER` | The round is over. `isMatchOver` says whether the match is too. |

After a round win:

- **Match not over** (best of 3): a "Player N wins the round!" banner shows for 2 s, then the
  next round starts with a fresh countdown and thumbs back at their start positions.
- **Match over**: after 1.5 s the game-over screen shows the winner, the round score,
  **REMATCH** (same mode, difficulty and match length) and **MAIN MENU**.

If the app goes to the background the game pauses, fingers are let go, and a round that would
have started in the background waits until you come back.

## Modes

**VS Computer.** Pick a difficulty, then Single Round or Best of 3. The whole screen controls
your thumb (P1).

**2 Players.** One device, forced to landscape. The side of the screen a finger first touches
(left = P1, right = P2) decides which thumb it controls, and it keeps controlling that thumb
even across the middle line. If a player has two fingers down and lifts one, the other one
takes over.

**Match length.** Single Round (first pin wins) or Best of 3 (first to 2 round wins).

## Computer Difficulty

Each difficulty re-decides after a reaction delay and sometimes makes a deliberate mistake
(a random nearby target).

| | Easy | Medium | Hard |
|---|---|---|---|
| Reaction delay | 400 ms | 200 ms | 80 ms |
| Mistake chance | 30% | 15% | 10% |
| Style | Wanders when far away, creeps closer when near; rests for 0.5–1.5 s on 15% of decisions | Approaches; within 0.3 mixes approaching and dodging; attacks within 0.15 | Predicts where you'll be 0.1 s ahead; attacks within 0.12; feints (dodges) 30% of the time within 0.25 |

On Medium and Hard, a pinned computer thumb tries to dodge away. The computer starts every
round fresh.

## Sound and Vibration

| Event | Sound | Vibration |
|---|---|---|
| Countdown number | beep | 30 ms |
| "I declare a thumb war!" | distinct beep | 50 ms |
| Pin starts | thud | 100 ms |
| Round won (match continues) | pin-complete cue | 150 ms |
| Match won | fanfare | pattern |

Sounds are Android system tones (no audio files). Both can be turned off in Settings.

## Settings and Statistics

**Settings:** sound effects, vibration, and a default difficulty (shown highlighted when you
pick a difficulty).

**Statistics** count matches **against the computer** only: wins, losses, current win streak and
best win streak. A result is recorded when a match finishes; matches abandoned with Back
aren't counted. 2-player matches aren't recorded. Stats can be reset in Settings.

## Planned Mechanics (Future)

- Power-ups during gameplay
- Multiple arena environments
- Combo multipliers for consecutive pins
- Special move animations
- Pressure mechanics (hand stamina)
