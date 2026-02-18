package com.thumbwar.engine

import com.thumbwar.util.Vector2
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        engine = GameEngine()
    }

    @Test
    fun `initial state is READY`() {
        val state = engine.getState()
        assertEquals(GamePhase.READY, state.phase)
    }

    @Test
    fun `startGame begins countdown`() {
        engine.startGame()
        val state = engine.getState()
        assertEquals(GamePhase.COUNTDOWN, state.phase)
    }

    @Test
    fun `thumbs start at correct positions`() {
        val state = engine.getState()
        assertEquals(GameConfig.PLAYER1_START_X, state.thumb1.position.x, 0.001f)
        assertEquals(GameConfig.PLAYER1_START_Y, state.thumb1.position.y, 0.001f)
        assertEquals(GameConfig.PLAYER2_START_X, state.thumb2.position.x, 0.001f)
        assertEquals(GameConfig.PLAYER2_START_Y, state.thumb2.position.y, 0.001f)
    }

    @Test
    fun `countdown completes and transitions to PLAYING`() {
        engine.startGame()

        // Tick through the entire countdown
        val totalCountdownMs = GameConfig.COUNTDOWN_BEAT_DURATION_MS * GameConfig.COUNTDOWN_BEATS +
                GameConfig.COUNTDOWN_DECLARE_DURATION_MS + 100
        val tickCount = (totalCountdownMs / GameConfig.TICK_RATE_MS).toInt() + 1

        repeat(tickCount) {
            engine.tick(GameConfig.TICK_RATE_MS)
        }

        assertEquals(GamePhase.PLAYING, engine.getState().phase)
    }

    @Test
    fun `player input ignored during countdown`() {
        engine.startGame()
        val initialPos = engine.getState().thumb1.position

        engine.setPlayer1Target(Vector2(0.9f, 0.9f))
        engine.tick(GameConfig.TICK_RATE_MS)

        val newPos = engine.getState().thumb1.position
        assertEquals(initialPos.x, newPos.x, 0.001f)
        assertEquals(initialPos.y, newPos.y, 0.001f)
    }

    @Test
    fun `player input moves thumb during PLAYING`() {
        engine.startGame()
        advanceToPlaying()

        val initialPos = engine.getState().thumb1.position
        engine.setPlayer1Target(Vector2(0.9f, 0.5f))

        // Tick several frames
        repeat(10) { engine.tick(GameConfig.TICK_RATE_MS) }

        val newPos = engine.getState().thumb1.position
        assertTrue("Thumb should have moved right", newPos.x > initialPos.x)
    }

    @Test
    fun `pin triggers when thumbs overlap with velocity`() {
        engine.startGame()
        advanceToPlaying()

        // Move both thumbs toward center
        val centerTarget = Vector2(0.5f, 0.5f)
        engine.setPlayer1Target(centerTarget)
        engine.setPlayer2Target(centerTarget)

        // Tick until they collide
        repeat(200) {
            engine.tick(GameConfig.TICK_RATE_MS)
        }

        val state = engine.getState()
        val phase = state.phase
        assertTrue(
            "Should be PIN_IN_PROGRESS or GAME_OVER, was $phase",
            phase == GamePhase.PIN_IN_PROGRESS || phase == GamePhase.GAME_OVER
        )
    }

    @Test
    fun `pin completes and game ends after pin duration`() {
        engine.startGame()
        advanceToPlaying()

        // Move thumbs together
        val target = Vector2(0.5f, 0.5f)
        engine.setPlayer1Target(target)
        engine.setPlayer2Target(target)

        // Tick enough for collision + full pin duration
        val totalTicks = ((GameConfig.PIN_DURATION_SECONDS * 1000 + 2000) / GameConfig.TICK_RATE_MS).toInt()
        repeat(totalTicks) {
            engine.tick(GameConfig.TICK_RATE_MS)
        }

        val state = engine.getState()
        assertEquals(GamePhase.GAME_OVER, state.phase)
        assertTrue("Winner should be 1 or 2", state.winner in 1..2)
    }

    @Test
    fun `resetRound resets all state`() {
        engine.startGame()
        advanceToPlaying()

        engine.resetRound()
        val state = engine.getState()

        assertEquals(GamePhase.READY, state.phase)
        assertEquals(0f, state.pinProgress, 0.001f)
        assertEquals(0, state.pinnerPlayer)
        assertEquals(0, state.winner)
        assertEquals(GameConfig.PLAYER1_START_X, state.thumb1.position.x, 0.001f)
        assertEquals(GameConfig.PLAYER2_START_X, state.thumb2.position.x, 0.001f)
    }

    @Test
    fun `scores increment on win`() {
        engine.startGame()
        advanceToPlaying()

        val target = Vector2(0.5f, 0.5f)
        engine.setPlayer1Target(target)
        engine.setPlayer2Target(target)

        val totalTicks = ((GameConfig.PIN_DURATION_SECONDS * 1000 + 2000) / GameConfig.TICK_RATE_MS).toInt()
        repeat(totalTicks) {
            engine.tick(GameConfig.TICK_RATE_MS)
        }

        val state = engine.getState()
        assertTrue("Total score should be 1", state.p1Score + state.p2Score == 1)
    }

    @Test
    fun `single round match is over after one pin`() {
        engine.startGame()
        advanceToPlaying()
        winRound()

        val state = engine.getState()
        assertEquals(GamePhase.GAME_OVER, state.phase)
        assertTrue(state.isMatchOver)
        assertEquals(1, state.p1RoundWins + state.p2RoundWins)
    }

    @Test
    fun `best of 3 match not over after first round`() {
        val bo3Engine = GameEngine(winsNeeded = 2)
        bo3Engine.startGame()
        advanceToPlaying(bo3Engine)
        winRound(bo3Engine)

        val state = bo3Engine.getState()
        assertEquals(GamePhase.GAME_OVER, state.phase)
        assertFalse("Match should not be over after 1 round", state.isMatchOver)
        assertEquals(1, state.roundNumber)
    }

    @Test
    fun `nextRound preserves round wins and increments round number`() {
        val bo3Engine = GameEngine(winsNeeded = 2)
        bo3Engine.startGame()
        advanceToPlaying(bo3Engine)
        winRound(bo3Engine)

        val stateAfterRound1 = bo3Engine.getState()
        val p1Wins = stateAfterRound1.p1RoundWins
        val p2Wins = stateAfterRound1.p2RoundWins

        bo3Engine.nextRound()
        val state = bo3Engine.getState()

        assertEquals(GamePhase.READY, state.phase)
        assertEquals(2, state.roundNumber)
        assertEquals(p1Wins, state.p1RoundWins)
        assertEquals(p2Wins, state.p2RoundWins)
        assertEquals(0, state.winner)
        assertEquals(0f, state.pinProgress, 0.001f)
    }

    @Test
    fun `best of 3 match ends when player reaches 2 wins`() {
        val bo3Engine = GameEngine(winsNeeded = 2)

        // Round 1
        bo3Engine.startGame()
        advanceToPlaying(bo3Engine)
        winRound(bo3Engine)
        val round1Winner = bo3Engine.getState().winner
        assertFalse(bo3Engine.getState().isMatchOver)

        // Round 2
        bo3Engine.nextRound()
        bo3Engine.startGame()
        advanceToPlaying(bo3Engine)
        winRound(bo3Engine)

        val state = bo3Engine.getState()
        if (state.winner == round1Winner) {
            // Same player won both — match should be over
            assertTrue("Match should be over when same player wins 2", state.isMatchOver)
        } else {
            // Different winners — match continues
            assertFalse("Match should not be over at 1-1", state.isMatchOver)
        }
    }

    @Test
    fun `resetRound clears round wins and round number`() {
        val bo3Engine = GameEngine(winsNeeded = 2)
        bo3Engine.startGame()
        advanceToPlaying(bo3Engine)
        winRound(bo3Engine)

        bo3Engine.resetRound()
        val state = bo3Engine.getState()

        assertEquals(0, state.p1RoundWins)
        assertEquals(0, state.p2RoundWins)
        assertEquals(1, state.roundNumber)
        assertFalse(state.isMatchOver)
    }

    @Test
    fun `winsNeeded is reflected in state`() {
        val bo3Engine = GameEngine(winsNeeded = 2)
        assertEquals(2, bo3Engine.getState().winsNeeded)

        val singleEngine = GameEngine(winsNeeded = 1)
        assertEquals(1, singleEngine.getState().winsNeeded)
    }

    private fun winRound(eng: GameEngine = engine) {
        val target = Vector2(0.5f, 0.5f)
        eng.setPlayer1Target(target)
        eng.setPlayer2Target(target)

        val totalTicks = ((GameConfig.PIN_DURATION_SECONDS * 1000 + 2000) / GameConfig.TICK_RATE_MS).toInt()
        repeat(totalTicks) {
            eng.tick(GameConfig.TICK_RATE_MS)
        }
        assertEquals(GamePhase.GAME_OVER, eng.getState().phase)
    }

    private fun advanceToPlaying(eng: GameEngine = engine) {
        val totalCountdownMs = GameConfig.COUNTDOWN_BEAT_DURATION_MS * GameConfig.COUNTDOWN_BEATS +
                GameConfig.COUNTDOWN_DECLARE_DURATION_MS + 200
        val tickCount = (totalCountdownMs / GameConfig.TICK_RATE_MS).toInt() + 1
        repeat(tickCount) {
            eng.tick(GameConfig.TICK_RATE_MS)
        }
        assertEquals(GamePhase.PLAYING, eng.getState().phase)
    }
}
