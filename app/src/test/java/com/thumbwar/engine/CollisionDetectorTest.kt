package com.thumbwar.engine

import com.thumbwar.util.Vector2
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CollisionDetectorTest {

    private lateinit var detector: CollisionDetector

    @Before
    fun setUp() {
        detector = CollisionDetector()
    }

    @Test
    fun `no pin when thumbs are far apart`() {
        val thumb1 = ThumbEntity(0.2f, 0.5f)
        val thumb2 = ThumbEntity(0.8f, 0.5f)
        val result = detector.checkPin(thumb1, thumb2)
        assertFalse(result.isPinning)
        assertEquals(0, result.pinnerPlayer)
    }

    @Test
    fun `pin detected when thumbs overlap`() {
        val thumb1 = ThumbEntity(0.5f, 0.5f)
        val thumb2 = ThumbEntity(0.5f + GameConfig.THUMB_RADIUS, 0.5f)

        // Give thumb1 velocity toward thumb2
        thumb1.setTarget(Vector2(0.8f, 0.5f))
        thumb1.update(0.016f)

        val result = detector.checkPin(thumb1, thumb2)
        assertTrue(result.isPinning)
        assertEquals(1, result.pinnerPlayer)
    }

    @Test
    fun `player 2 is pinner when moving faster toward player 1`() {
        val thumb1 = ThumbEntity(0.5f, 0.5f)
        val thumb2 = ThumbEntity(0.5f + GameConfig.THUMB_RADIUS * 0.5f, 0.5f)

        // Give thumb2 velocity toward thumb1
        thumb2.setTarget(Vector2(0.2f, 0.5f))
        thumb2.update(0.016f)

        val result = detector.checkPin(thumb1, thumb2)
        assertTrue(result.isPinning)
        assertEquals(2, result.pinnerPlayer)
    }

    @Test
    fun `no pin when just at threshold distance`() {
        val threshold = GameConfig.PIN_OVERLAP_THRESHOLD
        val thumb1 = ThumbEntity(0.5f, 0.5f)
        val thumb2 = ThumbEntity(0.5f + threshold + 0.01f, 0.5f)
        val result = detector.checkPin(thumb1, thumb2)
        assertFalse(result.isPinning)
    }

    @Test
    fun `pin when exactly at overlap threshold`() {
        val threshold = GameConfig.PIN_OVERLAP_THRESHOLD
        val thumb1 = ThumbEntity(0.5f, 0.5f)
        val thumb2 = ThumbEntity(0.5f + threshold * 0.5f, 0.5f)

        // Need some velocity to determine pinner
        thumb1.setTarget(Vector2(0.9f, 0.5f))
        thumb1.update(0.016f)

        val result = detector.checkPin(thumb1, thumb2)
        assertTrue(result.isPinning)
    }
}
