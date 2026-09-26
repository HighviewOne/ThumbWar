package com.thumbwar.engine

object GameConfig {
    // Thumb dimensions (world units: 1.0 = arena's shorter side, see ArenaScale)
    const val THUMB_RADIUS = 0.06f
    const val THUMB_SPEED = 0.8f // max units per second
    const val COAST_DRAG = 10f // per second: a released thumb keeps ~85% of its speed each 16 ms frame

    // Pin mechanics
    const val PIN_OVERLAP_THRESHOLD = THUMB_RADIUS * 2 // distance for pin detection
    const val PIN_ESCAPE_DISTANCE = THUMB_RADIUS * 2.5f // pinned thumb must get this far away to break free

    // Pinned thumb moves at this fraction of THUMB_SPEED: a pinner tracking closely holds on,
    // one whose finger trails by a reaction time (~250 ms) can be outrun
    const val PINNED_SPEED_FACTOR = 0.85f
    const val PIN_DURATION_SECONDS = 2.5f // seconds to hold pin for win

    // Countdown
    const val COUNTDOWN_BEATS = 4 // "1, 2, 3, 4"
    const val COUNTDOWN_BEAT_DURATION_MS = 800L
    const val COUNTDOWN_DECLARE_DURATION_MS = 1500L // "I declare a thumb war!"

    // Arena bounds (normalized, with padding for thumb radius)
    const val ARENA_MIN_X = 0.05f
    const val ARENA_MAX_X = 0.95f
    const val ARENA_MIN_Y = 0.05f
    const val ARENA_MAX_Y = 0.95f

    // Starting positions
    const val PLAYER1_START_X = 0.3f
    const val PLAYER1_START_Y = 0.5f
    const val PLAYER2_START_X = 0.7f
    const val PLAYER2_START_Y = 0.5f

    // Game tick rate
    const val TICK_RATE_MS = 16L // ~60fps

    // Round transitions
    const val ROUND_TRANSITION_DELAY_MS = 2000L
}
