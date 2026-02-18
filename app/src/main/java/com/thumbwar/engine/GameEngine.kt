package com.thumbwar.engine

import com.thumbwar.util.Vector2

class GameEngine(private val winsNeeded: Int = 1) {
    val thumb1 = ThumbEntity(GameConfig.PLAYER1_START_X, GameConfig.PLAYER1_START_Y)
    val thumb2 = ThumbEntity(GameConfig.PLAYER2_START_X, GameConfig.PLAYER2_START_Y)

    private val collisionDetector = CollisionDetector()
    val phaseManager = PhaseManager()

    private var pinProgress: Float = 0f
    private var pinnerPlayer: Int = 0
    private var p1Score: Int = 0
    private var p2Score: Int = 0
    private var winner: Int = 0
    private var elapsedTimeMs: Long = 0
    private var roundNumber: Int = 1
    private var p1RoundWins: Int = 0
    private var p2RoundWins: Int = 0
    private var isMatchOver: Boolean = false

    fun startGame() {
        phaseManager.startCountdown()
    }

    fun setPlayer1Target(target: Vector2) {
        if (phaseManager.phase == GamePhase.PLAYING || phaseManager.phase == GamePhase.PIN_IN_PROGRESS) {
            thumb1.setTarget(target)
        }
    }

    fun setPlayer2Target(target: Vector2) {
        if (phaseManager.phase == GamePhase.PLAYING || phaseManager.phase == GamePhase.PIN_IN_PROGRESS) {
            thumb2.setTarget(target)
        }
    }

    fun releasePlayer1() {
        thumb1.clearTarget()
    }

    fun releasePlayer2() {
        thumb2.clearTarget()
    }

    fun tick(deltaMs: Long) {
        elapsedTimeMs += deltaMs
        val deltaSeconds = deltaMs / 1000f

        when (phaseManager.phase) {
            GamePhase.READY -> { /* waiting for startGame() */ }
            GamePhase.COUNTDOWN -> phaseManager.updateCountdown(deltaMs)
            GamePhase.PLAYING -> updatePlaying(deltaSeconds)
            GamePhase.PIN_IN_PROGRESS -> updatePin(deltaSeconds)
            GamePhase.GAME_OVER -> { /* game ended */ }
        }
    }

    private fun updatePlaying(deltaSeconds: Float) {
        thumb1.update(deltaSeconds)
        thumb2.update(deltaSeconds)

        val pinResult = collisionDetector.checkPin(thumb1, thumb2)
        if (pinResult.isPinning) {
            pinnerPlayer = pinResult.pinnerPlayer
            pinProgress = 0f

            if (pinnerPlayer == 1) {
                thumb1.isPinning = true
                thumb2.isPinned = true
            } else {
                thumb2.isPinning = true
                thumb1.isPinned = true
            }

            phaseManager.startPin()
        }
    }

    private fun updatePin(deltaSeconds: Float) {
        thumb1.update(deltaSeconds)
        thumb2.update(deltaSeconds)

        val distance = thumb1.position.distanceTo(thumb2.position)

        // Check if pinned thumb escaped
        if (distance >= GameConfig.PIN_OVERLAP_THRESHOLD) {
            cancelPin()
            return
        }

        // Advance pin progress
        pinProgress += deltaSeconds / GameConfig.PIN_DURATION_SECONDS
        if (pinProgress >= 1f) {
            pinProgress = 1f
            // Pin complete — pinner wins the round
            if (pinnerPlayer == 1) { p1Score++; p1RoundWins++ } else { p2Score++; p2RoundWins++ }
            winner = pinnerPlayer
            isMatchOver = (if (pinnerPlayer == 1) p1RoundWins else p2RoundWins) >= winsNeeded
            phaseManager.gameOver()
        }
    }

    private fun cancelPin() {
        pinProgress = 0f
        pinnerPlayer = 0
        thumb1.isPinned = false
        thumb1.isPinning = false
        thumb2.isPinned = false
        thumb2.isPinning = false
        phaseManager.cancelPin()
    }

    fun resetRound() {
        thumb1.reset(GameConfig.PLAYER1_START_X, GameConfig.PLAYER1_START_Y)
        thumb2.reset(GameConfig.PLAYER2_START_X, GameConfig.PLAYER2_START_Y)
        pinProgress = 0f
        pinnerPlayer = 0
        winner = 0
        elapsedTimeMs = 0
        roundNumber = 1
        p1RoundWins = 0
        p2RoundWins = 0
        isMatchOver = false
        phaseManager.reset()
    }

    fun nextRound() {
        thumb1.reset(GameConfig.PLAYER1_START_X, GameConfig.PLAYER1_START_Y)
        thumb2.reset(GameConfig.PLAYER2_START_X, GameConfig.PLAYER2_START_Y)
        pinProgress = 0f
        pinnerPlayer = 0
        winner = 0
        elapsedTimeMs = 0
        roundNumber++
        phaseManager.reset()
    }

    fun getState(): GameState = GameState(
        phase = phaseManager.phase,
        thumb1 = thumb1.toState(),
        thumb2 = thumb2.toState(),
        pinProgress = pinProgress,
        pinnerPlayer = pinnerPlayer,
        countdownBeat = phaseManager.countdownBeat,
        countdownText = phaseManager.countdownText,
        p1Score = p1Score,
        p2Score = p2Score,
        winner = winner,
        elapsedTimeMs = elapsedTimeMs,
        roundNumber = roundNumber,
        p1RoundWins = p1RoundWins,
        p2RoundWins = p2RoundWins,
        winsNeeded = winsNeeded,
        isMatchOver = isMatchOver
    )
}
