package com.thumbwar.engine

import com.thumbwar.util.Vector2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `wide arena - no pin when thumbs are visibly apart horizontally`() {
        // Normalized gap 0.1 < threshold, but on a 2:1 screen that's 0.2 short-side units
        val scale = ArenaScale.fromSize(2000f, 1000f)
        val thumb1 = ThumbEntity(0.5f, 0.5f)
        val thumb2 = ThumbEntity(0.6f, 0.5f)
        thumb1.setTarget(Vector2(0.9f, 0.5f))
        thumb1.update(0.016f, scale)

        assertFalse(detector.checkPin(thumb1, thumb2, scale).isPinning)
    }

    @Test
    fun `tall arena - pin when thumbs visibly overlap vertically`() {
        // Normalized gap 0.07 on a 1:2 screen is 0.14 short-side units: no pin
        // Normalized gap 0.05 is 0.10 short-side units: pin
        val scale = ArenaScale.fromSize(1000f, 2000f)
        val thumb2Far = ThumbEntity(0.5f, 0.57f)
        val thumb2Near = ThumbEntity(0.5f, 0.55f)
        val thumb1 = ThumbEntity(0.5f, 0.5f)
        thumb1.setTarget(Vector2(0.5f, 0.9f))
        thumb1.update(0.001f, scale)

        assertFalse(detector.checkPin(thumb1, thumb2Far, scale).isPinning)
        assertTrue(detector.checkPin(thumb1, thumb2Near, scale).isPinning)
    }
}
