package com.thumbwar.input

import com.thumbwar.util.Vector2

class InputManager(
    private val isTwoPlayer: Boolean
) {
    // Maps pointer IDs to player numbers
    private val pointerToPlayer = mutableMapOf<Long, Int>()

    fun processPointerDown(pointerId: Long, x: Float, y: Float, canvasWidth: Float, canvasHeight: Float): InputEvent {
        val normalizedX = (x / canvasWidth).coerceIn(0f, 1f)
        val normalizedY = (y / canvasHeight).coerceIn(0f, 1f)
        val position = Vector2(normalizedX, normalizedY)

        val player = if (isTwoPlayer) {
            // Left half = P1, right half = P2
            if (normalizedX < 0.5f) 1 else 2
        } else {
            1  // Single player: all touch is P1
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
