package com.thumbwar.engine

import com.thumbwar.util.Vector2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThumbEntityTest {

    private val epsilon = 0.001f

    @Test
    fun `initial position matches constructor args`() {
        val thumb = ThumbEntity(0.3f, 0.5f)
        assertEquals(0.3f, thumb.position.x, epsilon)
        assertEquals(0.5f, thumb.position.y, epsilon)
    }

    @Test
    fun `thumb moves toward target`() {
        val thumb = ThumbEntity(0.3f, 0.5f)
        thumb.setTarget(Vector2(0.8f, 0.5f))
        thumb.update(0.1f)
        assertTrue("Thumb should move right", thumb.position.x > 0.3f)
    }

    @Test
    fun `thumb does not exceed max speed`() {
        val thumb = ThumbEntity(0.1f, 0.5f)
        thumb.setTarget(Vector2(0.9f, 0.5f))
        thumb.update(0.016f)

        val maxMove = GameConfig.THUMB_SPEED * 0.016f
        val distMoved = thumb.position.x - 0.1f
        assertTrue("Should not exceed max speed", distMoved <= maxMove + epsilon)
    }

    @Test
    fun `thumb stays within arena bounds`() {
        val thumb = ThumbEntity(0.95f, 0.95f)
        thumb.setTarget(Vector2(2f, 2f))
        thumb.update(0.5f)

        assertTrue(thumb.position.x <= GameConfig.ARENA_MAX_X + epsilon)
        assertTrue(thumb.position.y <= GameConfig.ARENA_MAX_Y + epsilon)
    }

    @Test
    fun `pinned thumb moves at reduced speed`() {
        val thumb = ThumbEntity(0.5f, 0.5f)
        thumb.isPinned = true
        thumb.setTarget(Vector2(0.9f, 0.5f))
        thumb.update(0.1f)

        val expected = GameConfig.THUMB_SPEED * GameConfig.PINNED_SPEED_FACTOR * 0.1f
        assertEquals(0.5f + expected, thumb.position.x, epsilon)
    }

    @Test
    fun `pinned thumb without target stays put`() {
        val thumb = ThumbEntity(0.5f, 0.5f)
        thumb.isPinned = true
        thumb.update(0.1f)

        assertEquals(0.5f, thumb.position.x, epsilon)
        assertEquals(0.5f, thumb.position.y, epsilon)
    }

    @Test
    fun `speed is uniform on screen in a wide arena`() {
        // 2:1 arena: one normalized x unit is twice as long on screen as one y unit
        val scale = ArenaScale.fromSize(2000f, 1000f)
        val horizontal = ThumbEntity(0.2f, 0.5f)
        horizontal.setTarget(Vector2(0.9f, 0.5f))
        horizontal.update(0.1f, scale)
        val vertical = ThumbEntity(0.5f, 0.1f)
        vertical.setTarget(Vector2(0.5f, 0.9f))
        vertical.update(0.1f, scale)

        val maxMove = GameConfig.THUMB_SPEED * 0.1f
        assertEquals(maxMove, (horizontal.position.x - 0.2f) * scale.x, epsilon)
        assertEquals(maxMove, (vertical.position.y - 0.1f) * scale.y, epsilon)
    }

    @Test
    fun `clearTarget causes deceleration`() {
        val thumb = ThumbEntity(0.5f, 0.5f)
        thumb.setTarget(Vector2(0.8f, 0.5f))
        thumb.update(0.016f) // build some velocity
        val velAfterMove = thumb.velocity.length()

        thumb.clearTarget()
        thumb.update(0.016f)
        val velAfterClear = thumb.velocity.length()

        assertTrue("Velocity should decrease after clearing target", velAfterClear < velAfterMove)
    }

    @Test
    fun `reset restores initial state`() {
        val thumb = ThumbEntity(0.3f, 0.5f)
        thumb.setTarget(Vector2(0.8f, 0.8f))
        thumb.update(0.1f)
        thumb.isPinned = true
        thumb.isPinning = true

        thumb.reset(0.3f, 0.5f)

        assertEquals(0.3f, thumb.position.x, epsilon)
        assertEquals(0.5f, thumb.position.y, epsilon)
        assertEquals(0f, thumb.velocity.length(), epsilon)
        assertFalse(thumb.isPinned)
        assertFalse(thumb.isPinning)
    }

    @Test
    fun `toState returns correct snapshot`() {
        val thumb = ThumbEntity(0.4f, 0.6f)
        thumb.isPinned = true

        val state = thumb.toState()
        assertEquals(0.4f, state.position.x, epsilon)
        assertEquals(0.6f, state.position.y, epsilon)
        assertTrue(state.isPinned)
        assertFalse(state.isPinning)
    }
}
