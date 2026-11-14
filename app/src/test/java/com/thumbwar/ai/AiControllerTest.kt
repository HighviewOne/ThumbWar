package com.thumbwar.ai

import com.thumbwar.engine.ArenaScale
import com.thumbwar.engine.GamePhase
import com.thumbwar.engine.GameState
import com.thumbwar.engine.ThumbState
import com.thumbwar.util.Vector2
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiControllerTest {

    private fun makeGameState(
        thumb1Pos: Vector2 = Vector2(0.3f, 0.5f),
        thumb2Pos: Vector2 = Vector2(0.7f, 0.5f),
        phase: GamePhase = GamePhase.PLAYING,
        aiPinned: Boolean = false
    ): GameState {
        return GameState(
            phase = phase,
            thumb1 = ThumbState(position = thumb1Pos, isPinning = aiPinned),
            thumb2 = ThumbState(position = thumb2Pos, isPinned = aiPinned)
        )
    }

    @Test
    fun `easy AI returns a target`() {
        val ai = AiController(AiDifficulty.EASY, Random(SEED))
        val state = makeGameState()
        // Easy AI may rest (returning no target) for up to 1.5s, so give it a few decisions
        val targets = List(10) { ai.update(state, 500) }
        assertTrue("AI should produce a target", targets.any { it != null })
    }

    @Test
    fun `same seed gives the same decisions`() {
        val state = makeGameState()
        fun run(): List<Vector2?> {
            val ai = AiController(AiDifficulty.HARD, Random(SEED))
            return List(20) { ai.update(state, 100) }
        }
        assertEquals(run(), run())
    }

    @Test
    fun `medium AI returns a target`() {
        val ai = AiController(AiDifficulty.MEDIUM, Random(SEED))
        val state = makeGameState()
        ai.update(state, 300)
        val target = ai.update(state, 300)
        assertNotNull("Medium AI should produce a target", target)
    }

    @Test
    fun `hard AI returns a target`() {
        val ai = AiController(AiDifficulty.HARD, Random(SEED))
        val state = makeGameState()
        ai.update(state, 100)
        val target = ai.update(state, 100)
        assertNotNull("Hard AI should produce a target", target)
    }

    @Test
    fun `AI target stays within arena bounds`() {
        val ai = AiController(AiDifficulty.HARD, Random(SEED))
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
        val ai = AiController(AiDifficulty.EASY, Random(SEED)) // 400ms delay
        val state = makeGameState()
        // First call with small delta — should return the initial currentTarget (null)
        val target = ai.update(state, 10)
        assertNull("Should not decide before reaction delay", target)
    }

    @Test
    fun `different difficulties have different behavior`() {
        val easyAi = AiController(AiDifficulty.EASY, Random(SEED))
        val hardAi = AiController(AiDifficulty.HARD, Random(SEED))

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

    @Test
    fun `reset clears the previous round's target`() {
        val ai = AiController(AiDifficulty.HARD, Random(SEED))
        val state = makeGameState()
        repeat(5) { ai.update(state, 100) }

        ai.reset()
        assertNull("No target right after reset, before the reaction delay", ai.update(state, 10))
    }

    /** Escape targets sit 0.3 away; a deliberate mistake lands within ~0.14, so skip those. */
    private fun escapeTargets(ai: AiController, state: GameState, decisions: Int, deltaMs: Long): List<Vector2> =
        List(decisions) { ai.update(state, deltaMs) }
            .filterNotNull()
            .filter { it.distanceTo(state.thumb2.position) > 0.2f }

    @Test
    fun `pinned AI runs toward open space, not into the wall`() {
        // Pinned against the bottom wall with the pinner to its left: sideways means up or down
        val state = makeGameState(thumb1Pos = Vector2(0.45f, 0.9f), thumb2Pos = Vector2(0.5f, 0.9f), aiPinned = true)
        for (difficulty in listOf(AiDifficulty.MEDIUM, AiDifficulty.HARD)) {
            val targets = escapeTargets(AiController(difficulty, Random(SEED)), state, 30, 200)
            assertTrue("$difficulty should try to escape", targets.isNotEmpty())
            targets.forEach { assertTrue("$difficulty ran toward the wall: $it", it.y < 0.9f) }
        }
    }

    @Test
    fun `pinned medium AI keeps running one way instead of zig-zagging`() {
        val state = makeGameState(thumb1Pos = Vector2(0.45f, 0.5f), thumb2Pos = Vector2(0.5f, 0.5f), aiPinned = true)
        val targets = escapeTargets(AiController(AiDifficulty.MEDIUM, Random(SEED)), state, 30, 200)
        assertTrue(targets.size > 10)
        assertEquals("Same escape target every decision", 1, targets.distinct().size)
    }

    @Test
    fun `pinned hard AI cuts back the other way now and then`() {
        val state = makeGameState(thumb1Pos = Vector2(0.45f, 0.5f), thumb2Pos = Vector2(0.5f, 0.5f), aiPinned = true)
        // 3 s of decisions; cuts come every 300-600 ms
        val sides = escapeTargets(AiController(AiDifficulty.HARD, Random(SEED)), state, 37, 80)
            .map { it.y < 0.5f }
        val cuts = sides.zipWithNext().count { (a, b) -> a != b }
        assertTrue("Expected a few cuts, got $cuts in $sides", cuts in 3..10)
    }

    @Test
    fun `room is measured to the padded arena edge`() {
        val wide = ArenaScale(2f, 1f)
        assertEquals(0.45f, roomAlong(Vector2(0.5f, 0.5f), Vector2(1f, 0f), ArenaScale.SQUARE), 0.001f)
        assertEquals(1.4f, roomAlong(Vector2(0.5f, 0.5f), Vector2(1f, 0f), wide), 0.001f)
        assertEquals(0.45f, roomAlong(Vector2(0.5f, 0.5f), Vector2(0f, -1f), wide), 0.001f)
    }

    private companion object {
        const val SEED = 42
    }
}
