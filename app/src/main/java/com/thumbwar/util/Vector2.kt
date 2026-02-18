package com.thumbwar.util

import kotlin.math.sqrt

data class Vector2(val x: Float = 0f, val y: Float = 0f) {

    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vector2(x / scalar, y / scalar)

    fun length(): Float = sqrt(x * x + y * y)

    fun lengthSquared(): Float = x * x + y * y

    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0.0001f) Vector2(x / len, y / len) else ZERO
    }

    fun dot(other: Vector2): Float = x * other.x + y * other.y

    fun distanceTo(other: Vector2): Float = (this - other).length()

    fun clamp(minX: Float, minY: Float, maxX: Float, maxY: Float): Vector2 {
        return Vector2(
            x.coerceIn(minX, maxX),
            y.coerceIn(minY, maxY)
        )
    }

    fun lerp(target: Vector2, t: Float): Vector2 {
        return this + (target - this) * t.coerceIn(0f, 1f)
    }

    companion object {
        val ZERO = Vector2(0f, 0f)
    }
}
