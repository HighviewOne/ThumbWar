package com.thumbwar.engine

import com.thumbwar.util.Vector2

class ThumbEntity(
    startX: Float,
    startY: Float
) {
    var position: Vector2 = Vector2(startX, startY)
        private set
    var velocity: Vector2 = Vector2.ZERO
        private set
    var isPinned: Boolean = false
    var isPinning: Boolean = false

    private var targetPosition: Vector2? = null

    fun setTarget(target: Vector2) {
        targetPosition = target
    }

    fun clearTarget() {
        targetPosition = null
    }

    fun update(deltaSeconds: Float) {
        val target = targetPosition
        if (target != null && !isPinned) {
            val diff = target - position
            val dist = diff.length()
            if (dist > 0.001f) {
                val maxMove = GameConfig.THUMB_SPEED * deltaSeconds
                val move = if (dist <= maxMove) diff else diff.normalized() * maxMove
                velocity = move / deltaSeconds
                position = position + move
            } else {
                velocity = Vector2.ZERO
            }
        } else if (!isPinned) {
            // Decelerate when no target
            velocity = velocity * 0.85f
            if (velocity.length() < 0.01f) {
                velocity = Vector2.ZERO
            }
            position = position + velocity * deltaSeconds
        } else {
            velocity = Vector2.ZERO
        }

        // Clamp to arena bounds
        position = position.clamp(
            GameConfig.ARENA_MIN_X, GameConfig.ARENA_MIN_Y,
            GameConfig.ARENA_MAX_X, GameConfig.ARENA_MAX_Y
        )
    }

    fun reset(startX: Float, startY: Float) {
        position = Vector2(startX, startY)
        velocity = Vector2.ZERO
        isPinned = false
        isPinning = false
        targetPosition = null
    }

    fun toState(): ThumbState = ThumbState(
        position = position,
        velocity = velocity,
        isPinned = isPinned,
        isPinning = isPinning
    )
}
