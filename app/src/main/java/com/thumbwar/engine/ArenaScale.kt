package com.thumbwar.engine

import com.thumbwar.util.Vector2

/**
 * Converts between normalized positions (0..1 on each axis) and world units, where one
 * world unit is the arena's shorter side. Distances and speeds in [GameConfig] are in world
 * units, so they match what is drawn on screen regardless of the arena's aspect ratio.
 */
data class ArenaScale(val x: Float = 1f, val y: Float = 1f) {

    fun toWorld(v: Vector2): Vector2 = Vector2(v.x * x, v.y * y)

    fun toNormalized(v: Vector2): Vector2 = Vector2(v.x / x, v.y / y)

    fun distance(a: Vector2, b: Vector2): Float = toWorld(a - b).length()

    companion object {
        val SQUARE = ArenaScale()

        fun fromSize(width: Float, height: Float): ArenaScale {
            if (width <= 0f || height <= 0f) return SQUARE
            val shortSide = minOf(width, height)
            return ArenaScale(width / shortSide, height / shortSide)
        }
    }
}
