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

## Building from Source

```
git clone https://github.com/HighviewOne/ThumbWar.git
cd ThumbWar
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Tech Stack

- Kotlin + Jetpack Compose
- Procedural Canvas rendering (no bitmap assets)
- Coroutine-based game loop (~60fps)
- DataStore for settings and stats persistence
- Material3 theming
