package com.thumbwar.engine

class CollisionDetector {

    data class PinResult(
        val isPinning: Boolean,
        val pinnerPlayer: Int  // 1 or 2, 0 if no pin
    )

    fun checkPin(thumb1: ThumbEntity, thumb2: ThumbEntity): PinResult {
        val distance = thumb1.position.distanceTo(thumb2.position)

        if (distance >= GameConfig.PIN_OVERLAP_THRESHOLD) {
            return PinResult(isPinning = false, pinnerPlayer = 0)
        }

        // Determine pinner by velocity toward opponent
        val toThumb2 = (thumb2.position - thumb1.position).normalized()
        val toThumb1 = (thumb1.position - thumb2.position).normalized()

        val p1VelocityToward = thumb1.velocity.dot(toThumb2)
        val p2VelocityToward = thumb2.velocity.dot(toThumb1)

        // The thumb moving faster toward the other is the pinner
        // If both are similar, the one on top (moving more recently) wins
        val pinner = when {
            p1VelocityToward > p2VelocityToward + 0.05f -> 1
            p2VelocityToward > p1VelocityToward + 0.05f -> 2
            // Tiebreaker: higher velocity magnitude
            thumb1.velocity.length() > thumb2.velocity.length() -> 1
            thumb2.velocity.length() > thumb1.velocity.length() -> 2
            else -> 0  // truly equal — no pin
        }

        return PinResult(isPinning = pinner != 0, pinnerPlayer = pinner)
    }
}
