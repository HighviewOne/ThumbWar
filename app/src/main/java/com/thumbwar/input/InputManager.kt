package com.thumbwar.input

import com.thumbwar.util.Vector2

class InputManager(
    private val isTwoPlayer: Boolean
) {
    // Maps pointer IDs to player numbers
    private val pointerToPlayer = mutableMapOf<Long, Int>()

    /**
     * Handles one pointer from a touch event. A single event lists every finger on the screen,
     * not just the one that changed, so each pointer is classified by its own pressed state
     * rather than by the event's overall type.
     */
    fun processPointer(
        pointerId: Long,
        x: Float,
        y: Float,
        pressed: Boolean,
        previousPressed: Boolean,
        canvasWidth: Float,
        canvasHeight: Float
    ): InputEvent? = when {
        pressed && !previousPressed -> processPointerDown(pointerId, x, y, canvasWidth, canvasHeight)
        !pressed && previousPressed -> processPointerUp(pointerId)
        pressed -> processPointerMove(pointerId, x, y, canvasWidth, canvasHeight)
        else -> null
    }

    fun processPointerDown(pointerId: Long, x: Float, y: Float, canvasWidth: Float, canvasHeight: Float): InputEvent {
        val normalizedX = (x / canvasWidth).coerceIn(0f, 1f)
        val normalizedY = (y / canvasHeight).coerceIn(0f, 1f)
        val position = Vector2(normalizedX, normalizedY)

        val player = if (isTwoPlayer) {
            // Left half = P1, right half = P2
            if (normalizedX < 0.5f) 1 else 2
        } else {
            1 // Single player: all touch is P1
        }

        pointerToPlayer[pointerId] = player
        return InputEvent.Move(player, position)
    }

    fun processPointerMove(pointerId: Long, x: Float, y: Float, canvasWidth: Float, canvasHeight: Float): InputEvent? {
        val player = pointerToPlayer[pointerId] ?: return null
        val normalizedX = (x / canvasWidth).coerceIn(0f, 1f)
        val normalizedY = (y / canvasHeight).coerceIn(0f, 1f)
        return InputEvent.Move(player, Vector2(normalizedX, normalizedY))
    }

    fun processPointerUp(pointerId: Long): InputEvent? {
        val player = pointerToPlayer.remove(pointerId) ?: return null
        return InputEvent.Release(player)
    }

    fun reset() {
        pointerToPlayer.clear()
    }
}
