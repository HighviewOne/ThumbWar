package com.thumbwar.engine

class PhaseManager {
    var phase: GamePhase = GamePhase.READY
        private set
    var countdownBeat: Int = 0
        private set
    var countdownText: String = ""
        private set

    private var countdownElapsedMs: Long = 0
    private var countdownDeclarePhase = false

    private val beatTexts = listOf("1", "2", "3", "4")
    private val declareText = "I declare a thumb war!"

    fun startCountdown() {
        phase = GamePhase.COUNTDOWN
        countdownBeat = 0
        countdownText = ""
        countdownElapsedMs = 0
        countdownDeclarePhase = false
    }

    fun updateCountdown(deltaMs: Long): Boolean {
        if (phase != GamePhase.COUNTDOWN) return false

        countdownElapsedMs += deltaMs

        if (!countdownDeclarePhase) {
            val beatIndex = (countdownElapsedMs / GameConfig.COUNTDOWN_BEAT_DURATION_MS).toInt()
            if (beatIndex < GameConfig.COUNTDOWN_BEATS) {
                countdownBeat = beatIndex + 1
                countdownText = beatTexts[beatIndex]
            } else {
                countdownDeclarePhase = true
                countdownElapsedMs = 0
                countdownText = declareText
            }
        } else {
            if (countdownElapsedMs >= GameConfig.COUNTDOWN_DECLARE_DURATION_MS) {
                phase = GamePhase.PLAYING
                countdownText = ""
                return true  // countdown finished
            }
        }
        return false
    }

    fun startPlaying() {
        phase = GamePhase.PLAYING
    }

    fun startPin() {
        phase = GamePhase.PIN_IN_PROGRESS
    }

    fun cancelPin() {
        phase = GamePhase.PLAYING
    }

    fun gameOver() {
        phase = GamePhase.GAME_OVER
    }

    fun reset() {
        phase = GamePhase.READY
        countdownBeat = 0
        countdownText = ""
        countdownElapsedMs = 0
        countdownDeclarePhase = false
    }
}
