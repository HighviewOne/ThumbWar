package com.thumbwar.input

import com.thumbwar.util.Vector2

class InputManager(
    private val isTwoPlayer: Boolean
) {
    private class TrackedPointer(val player: Int, var position: Vector2)

    // Fingers currently down, by pointer ID, in the order they were pressed
    private val pointers = linkedMapOf<Long, TrackedPointer>()

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

        pointers[pointerId] = TrackedPointer(player, position)
        return InputEvent.Move(player, position)
    }

    fun processPointerMove(pointerId: Long, x: Float, y: Float, canvasWidth: Float, canvasHeight: Float): InputEvent? {
        val pointer = pointers[pointerId] ?: return null
        val normalizedX = (x / canvasWidth).coerceIn(0f, 1f)
        val normalizedY = (y / canvasHeight).coerceIn(0f, 1f)
        pointer.position = Vector2(normalizedX, normalizedY)
        return InputEvent.Move(pointer.player, pointer.position)
    }

    fun processPointerUp(pointerId: Long): InputEvent? {
        val player = pointers.remove(pointerId)?.player ?: return null
        // If the player still has another finger down, that finger takes over instead of letting go
        val remaining = pointers.values.lastOrNull { it.player == player }
        return if (remaining != null) InputEvent.Move(player, remaining.position) else InputEvent.Release(player)
    }

    fun reset() {
        pointers.clear()
    }
}
