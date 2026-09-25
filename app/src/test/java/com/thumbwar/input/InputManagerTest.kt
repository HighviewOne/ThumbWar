package com.thumbwar.input

import com.thumbwar.util.Vector2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InputManagerTest {

    private val width = 1000f
    private val height = 500f

    private fun InputManager.down(id: Long, x: Float, y: Float = 250f) =
        processPointer(id, x, y, pressed = true, previousPressed = false, canvasWidth = width, canvasHeight = height)

    private fun InputManager.held(id: Long, x: Float, y: Float = 250f) =
        processPointer(id, x, y, pressed = true, previousPressed = true, canvasWidth = width, canvasHeight = height)

    private fun InputManager.up(id: Long, x: Float, y: Float = 250f) =
        processPointer(id, x, y, pressed = false, previousPressed = true, canvasWidth = width, canvasHeight = height)

    @Test
    fun `touch down produces a move with normalized position`() {
        val input = InputManager(isTwoPlayer = false)
        assertEquals(InputEvent.Move(1, Vector2(0.25f, 0.5f)), input.down(1, 250f))
    }

    @Test
    fun `positions outside the canvas are clamped`() {
        val input = InputManager(isTwoPlayer = false)
        assertEquals(InputEvent.Move(1, Vector2(1f, 0f)), input.down(1, 1500f, -20f))
    }

    @Test
    fun `touch up releases and stops tracking`() {
        val input = InputManager(isTwoPlayer = false)
        input.down(1, 250f)
        assertEquals(InputEvent.Release(1), input.up(1, 250f))
        assertNull(input.held(1, 300f))
    }

    @Test
    fun `two player - side of the first touch picks the player`() {
        val input = InputManager(isTwoPlayer = true)
        assertEquals(1, (input.down(1, 200f) as InputEvent.Move).player)
        assertEquals(2, (input.down(2, 800f) as InputEvent.Move).player)
    }

    @Test
    fun `two player - finger keeps its player after crossing the midline`() {
        val input = InputManager(isTwoPlayer = true)
        input.down(1, 200f)
        assertEquals(1, (input.held(1, 900f) as InputEvent.Move).player)
    }

    @Test
    fun `two player - other player's finger landing does not reassign an existing finger`() {
        val input = InputManager(isTwoPlayer = true)
        input.down(1, 200f)
        input.held(1, 700f) // P1 drags into P2's half

        // P2 presses: the event lists both fingers, P1's as still held
        input.down(2, 900f)
        val p1 = input.held(1, 700f) as InputEvent.Move
        assertEquals(1, p1.player)
    }

    @Test
    fun `two player - one player lifting does not release the other`() {
        val input = InputManager(isTwoPlayer = true)
        input.down(1, 200f)
        input.down(2, 800f)

        // P2 lifts: the event lists both fingers, P1's as still held
        val p1 = input.held(1, 210f)
        val p2 = input.up(2, 800f)

        assertEquals(InputEvent.Move(1, Vector2(0.21f, 0.5f)), p1)
        assertEquals(InputEvent.Release(2), p2)
        // P1 remains tracked afterwards
        assertEquals(1, (input.held(1, 220f) as InputEvent.Move).player)
    }

    @Test
    fun `single player - every touch controls player 1`() {
        val input = InputManager(isTwoPlayer = false)
        assertEquals(1, (input.down(1, 900f) as InputEvent.Move).player)
    }

    @Test
    fun `hover without press is ignored`() {
        val input = InputManager(isTwoPlayer = false)
        assertNull(
            input.processPointer(1, 100f, 100f, pressed = false, previousPressed = false, canvasWidth = width, canvasHeight = height)
        )
    }

    @Test
    fun `reset forgets all pointers`() {
        val input = InputManager(isTwoPlayer = true)
        input.down(1, 200f)
        input.reset()
        assertNull(input.held(1, 200f))
    }
}
