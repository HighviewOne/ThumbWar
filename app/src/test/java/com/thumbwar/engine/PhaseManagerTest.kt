package com.thumbwar.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PhaseManagerTest {

    private lateinit var phaseManager: PhaseManager

    @Before
    fun setUp() {
        phaseManager = PhaseManager()
    }

    @Test
    fun `initial phase is READY`() {
        assertEquals(GamePhase.READY, phaseManager.phase)
    }

    @Test
    fun `startCountdown sets phase to COUNTDOWN`() {
        phaseManager.startCountdown()
        assertEquals(GamePhase.COUNTDOWN, phaseManager.phase)
    }

    @Test
    fun `countdown progresses through beats`() {
        phaseManager.startCountdown()

        // Advance past first beat
        phaseManager.updateCountdown(GameConfig.COUNTDOWN_BEAT_DURATION_MS + 1)
        assertEquals(2, phaseManager.countdownBeat)
        assertEquals("2", phaseManager.countdownText)
    }

    @Test
    fun `countdown shows declare text after 4 beats`() {
        phaseManager.startCountdown()

        // Advance past all 4 beats
        val totalBeatTime = GameConfig.COUNTDOWN_BEAT_DURATION_MS * GameConfig.COUNTDOWN_BEATS
        phaseManager.updateCountdown(totalBeatTime + 1)
        assertEquals("I declare a thumb war!", phaseManager.countdownText)
    }

    @Test
    fun `countdown transitions to PLAYING after declare`() {
        phaseManager.startCountdown()

        // Advance past all beats
        val totalBeatTime = GameConfig.COUNTDOWN_BEAT_DURATION_MS * GameConfig.COUNTDOWN_BEATS
        phaseManager.updateCountdown(totalBeatTime + 1)

        // Advance past declare duration
        val finished = phaseManager.updateCountdown(GameConfig.COUNTDOWN_DECLARE_DURATION_MS + 1)
        assertTrue(finished)
        assertEquals(GamePhase.PLAYING, phaseManager.phase)
    }

    @Test
    fun `startPin changes phase`() {
        phaseManager.startPlaying()
        phaseManager.startPin()
        assertEquals(GamePhase.PIN_IN_PROGRESS, phaseManager.phase)
    }

    @Test
    fun `cancelPin returns to PLAYING`() {
        phaseManager.startPlaying()
        phaseManager.startPin()
        phaseManager.cancelPin()
        assertEquals(GamePhase.PLAYING, phaseManager.phase)
    }

    @Test
    fun `gameOver sets phase`() {
        phaseManager.startPlaying()
        phaseManager.gameOver()
        assertEquals(GamePhase.GAME_OVER, phaseManager.phase)
    }

    @Test
    fun `reset returns to READY`() {
        phaseManager.startCountdown()
        phaseManager.reset()
        assertEquals(GamePhase.READY, phaseManager.phase)
        assertEquals(0, phaseManager.countdownBeat)
        assertEquals("", phaseManager.countdownText)
    }
}
