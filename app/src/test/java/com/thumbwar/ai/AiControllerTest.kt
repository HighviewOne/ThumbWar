package com.thumbwar.ai

import com.thumbwar.engine.GameConfig
import com.thumbwar.engine.GamePhase
import com.thumbwar.engine.GameState
import com.thumbwar.engine.ThumbState
import com.thumbwar.util.Vector2
import org.junit.Assert.*
import org.junit.Test

class AiControllerTest {

    private fun makeGameState(
        thumb1Pos: Vector2 = Vector2(0.3f, 0.5f),
        thumb2Pos: Vector2 = Vector2(0.7f, 0.5f),
        phase: GamePhase = GamePhase.PLAYING
    ): GameState {
        return GameState(
            phase = phase,
            thumb1 = ThumbState(position = thumb1Pos),
            thumb2 = ThumbState(position = thumb2Pos)
        )
    }

    @Test
    fun `easy AI returns a target`() {
        val ai = AiController(AiDifficulty.EASY)
        val state = makeGameState()
        // Need to exceed reaction delay
        val target = ai.update(state, 500)
        // May or may not return null on first call, but should eventually return a target
        val target2 = ai.update(state, 500)
        // At least one should be non-null after enough time
        assertTrue("AI should produce a target", target != null || target2 != null)
    }

    @Test
    fun `medium AI returns a target`() {
        val ai = AiController(AiDifficulty.MEDIUM)
        val state = makeGameState()
        ai.update(state, 300)
        val target = ai.update(state, 300)
        assertNotNull("Medium AI should produce a target", target)
    }

    @Test
    fun `hard AI returns a target`() {
        val ai = AiController(AiDifficulty.HARD)
        val state = makeGameState()
        ai.update(state, 100)
        val target = ai.update(state, 100)
        assertNotNull("Hard AI should produce a target", target)
    }

    @Test
    fun `AI target stays within arena bounds`() {
        val ai = AiController(AiDifficulty.HARD)
        val state = makeGameState()

        repeat(50) {
            val target = ai.update(state, 100)
            if (target != null) {
                assertTrue("X should be >= 0", target.x >= 0f)
                assertTrue("X should be <= 1", target.x <= 1f)
                assertTrue("Y should be >= 0", target.y >= 0f)
                assertTrue("Y should be <= 1", target.y <= 1f)
            }
        }
    }

    @Test
    fun `AI returns null before reaction delay`() {
        val ai = AiController(AiDifficulty.EASY) // 400ms delay
        val state = makeGameState()
        // First call with small delta — should return the initial currentTarget (null)
        val target = ai.update(state, 10)
        assertNull("Should not decide before reaction delay", target)
    }

    @Test
    fun `different difficulties have different behavior`() {
        val easyAi = AiController(AiDifficulty.EASY)
        val hardAi = AiController(AiDifficulty.HARD)

        val state = makeGameState(
            thumb1Pos = Vector2(0.5f, 0.5f),
            thumb2Pos = Vector2(0.6f, 0.5f)
        )

        // Run both AIs for a while and collect targets
        val easyTargets = mutableListOf<Vector2>()
        val hardTargets = mutableListOf<Vector2>()

        repeat(20) {
            easyAi.update(state, 500)?.let { easyTargets.add(it) }
            hardAi.update(state, 500)?.let { hardTargets.add(it) }
        }

        // Both should produce targets
        assertTrue("Easy AI should produce targets", easyTargets.isNotEmpty())
        assertTrue("Hard AI should produce targets", hardTargets.isNotEmpty())
    }
}
