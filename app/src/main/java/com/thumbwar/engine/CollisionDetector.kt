package com.thumbwar.engine

class CollisionDetector {

    data class PinResult(
        val isPinning: Boolean,
        // 1 or 2, 0 if no pin
        val pinnerPlayer: Int
    )

    fun checkPin(thumb1: ThumbEntity, thumb2: ThumbEntity, scale: ArenaScale = ArenaScale.SQUARE): PinResult {
        val distance = scale.distance(thumb1.position, thumb2.position)

        if (distance >= GameConfig.PIN_OVERLAP_THRESHOLD) {
            return PinResult(isPinning = false, pinnerPlayer = 0)
        }

        // Determine pinner by velocity toward opponent (in world units)
        val toThumb2 = scale.toWorld(thumb2.position - thumb1.position).normalized()
        val toThumb1 = toThumb2 * -1f
        val velocity1 = scale.toWorld(thumb1.velocity)
        val velocity2 = scale.toWorld(thumb2.velocity)

        val p1VelocityToward = velocity1.dot(toThumb2)
        val p2VelocityToward = velocity2.dot(toThumb1)

        // The thumb moving faster toward the other is the pinner
        // If both are similar, the one on top (moving more recently) wins
        val pinner = when {
            p1VelocityToward > p2VelocityToward + 0.05f -> 1
            p2VelocityToward > p1VelocityToward + 0.05f -> 2
            // Tiebreaker: higher velocity magnitude
            velocity1.length() > velocity2.length() -> 1
            velocity2.length() > velocity1.length() -> 2
            else -> 0 // truly equal — no pin
        }

        return PinResult(isPinning = pinner != 0, pinnerPlayer = pinner)
    }
}
