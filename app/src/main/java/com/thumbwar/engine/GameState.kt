package com.thumbwar.engine

import com.thumbwar.util.Vector2

enum class GamePhase {
    READY,
    COUNTDOWN,
    PLAYING,
    PIN_IN_PROGRESS,
    GAME_OVER
}

data class ThumbState(
    val position: Vector2,
    val velocity: Vector2 = Vector2.ZERO,
    val isPinned: Boolean = false,
    val isPinning: Boolean = false
)

data class GameState(
    val phase: GamePhase = GamePhase.READY,
    val thumb1: ThumbState = ThumbState(
        position = Vector2(GameConfig.PLAYER1_START_X, GameConfig.PLAYER1_START_Y)
    ),
    val thumb2: ThumbState = ThumbState(
        position = Vector2(GameConfig.PLAYER2_START_X, GameConfig.PLAYER2_START_Y)
    ),
    val pinProgress: Float = 0f,  // 0..1
    val pinnerPlayer: Int = 0,     // 0 = none, 1 or 2
    val countdownBeat: Int = 0,    // 0..4, where 0 = not started
    val countdownText: String = "",
    val p1Score: Int = 0,
    val p2Score: Int = 0,
    val winner: Int = 0,           // 0 = no winner yet
    val elapsedTimeMs: Long = 0,
    val roundNumber: Int = 1,
    val p1RoundWins: Int = 0,
    val p2RoundWins: Int = 0,
    val winsNeeded: Int = 1,
    val isMatchOver: Boolean = false
)
