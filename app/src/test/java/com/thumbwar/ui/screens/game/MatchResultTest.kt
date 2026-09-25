package com.thumbwar.ui.screens.game

import com.thumbwar.engine.GamePhase
import com.thumbwar.engine.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatchResultTest {

    private val matchOver = GameState(phase = GamePhase.GAME_OVER, isMatchOver = true)

    @Test
    fun `human winning the match is a win`() {
        assertEquals(true, singlePlayerMatchResult(matchOver.copy(winner = 1), isTwoPlayer = false))
    }

    @Test
    fun `AI winning the match is a loss`() {
        assertEquals(false, singlePlayerMatchResult(matchOver.copy(winner = 2), isTwoPlayer = false))
    }

    @Test
    fun `end of a round that doesn't finish the match records nothing`() {
        val roundOver = matchOver.copy(winner = 1, isMatchOver = false, winsNeeded = 2)
        assertNull(singlePlayerMatchResult(roundOver, isTwoPlayer = false))
    }

    @Test
    fun `two player matches record nothing`() {
        assertNull(singlePlayerMatchResult(matchOver.copy(winner = 1), isTwoPlayer = true))
    }

    @Test
    fun `nothing is recorded before game over`() {
        assertNull(singlePlayerMatchResult(GameState(phase = GamePhase.PLAYING), isTwoPlayer = false))
    }
}
