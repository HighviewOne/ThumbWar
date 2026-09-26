package com.thumbwar.ui.screens.game

import com.thumbwar.audio.GameSound
import com.thumbwar.engine.GameConfig
import com.thumbwar.engine.GamePhase
import com.thumbwar.engine.PhaseManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountdownSoundTest {

    @Test
    fun `full countdown plays four beats then the declare cue once`() {
        val phaseManager = PhaseManager()
        phaseManager.startCountdown()

        val sounds = mutableListOf<GameSound>()
        var lastText = ""
        while (phaseManager.phase == GamePhase.COUNTDOWN) {
            phaseManager.updateCountdown(GameConfig.TICK_RATE_MS)
            countdownSoundFor(lastText, phaseManager.countdownText)?.let { sounds += it }
            lastText = phaseManager.countdownText
        }

        assertEquals(List(GameConfig.COUNTDOWN_BEATS) { GameSound.COUNTDOWN_BEAT } + GameSound.COUNTDOWN_DECLARE, sounds)
    }

    @Test
    fun `pin tick interval shrinks from slow to fast as the pin fills`() {
        assertEquals(450L, pinTickIntervalMs(0f))
        assertEquals(120L, pinTickIntervalMs(1f))
        assertEquals(285L, pinTickIntervalMs(0.5f))
        assertEquals(120L, pinTickIntervalMs(1.5f)) // clamped
    }

    @Test
    fun `no sound when text is unchanged or cleared`() {
        assertNull(countdownSoundFor("3", "3"))
        assertNull(countdownSoundFor("I declare a thumb war!", ""))
    }
}
