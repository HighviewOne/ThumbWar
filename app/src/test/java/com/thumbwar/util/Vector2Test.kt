package com.thumbwar.util

import org.junit.Assert.*
import org.junit.Test

class Vector2Test {

    private val epsilon = 0.001f

    @Test
    fun `addition works correctly`() {
        val a = Vector2(1f, 2f)
        val b = Vector2(3f, 4f)
        val result = a + b
        assertEquals(4f, result.x, epsilon)
        assertEquals(6f, result.y, epsilon)
    }

    @Test
    fun `subtraction works correctly`() {
        val a = Vector2(5f, 7f)
        val b = Vector2(2f, 3f)
        val result = a - b
        assertEquals(3f, result.x, epsilon)
        assertEquals(4f, result.y, epsilon)
    }

    @Test
    fun `scalar multiplication works`() {
        val v = Vector2(3f, 4f)
        val result = v * 2f
        assertEquals(6f, result.x, epsilon)
        assertEquals(8f, result.y, epsilon)
    }

    @Test
    fun `scalar division works`() {
        val v = Vector2(6f, 8f)
        val result = v / 2f
        assertEquals(3f, result.x, epsilon)
        assertEquals(4f, result.y, epsilon)
    }

    @Test
    fun `length of 3-4-5 triangle`() {
        val v = Vector2(3f, 4f)
        assertEquals(5f, v.length(), epsilon)
    }

    @Test
    fun `length of zero vector`() {
        assertEquals(0f, Vector2.ZERO.length(), epsilon)
    }

    @Test
    fun `normalized vector has length 1`() {
        val v = Vector2(3f, 4f).normalized()
        assertEquals(1f, v.length(), epsilon)
    }

    @Test
    fun `normalized zero vector returns zero`() {
        val v = Vector2.ZERO.normalized()
        assertEquals(0f, v.x, epsilon)
        assertEquals(0f, v.y, epsilon)
    }

    @Test
    fun `dot product of perpendicular vectors is zero`() {
        val a = Vector2(1f, 0f)
        val b = Vector2(0f, 1f)
        assertEquals(0f, a.dot(b), epsilon)
    }

    @Test
    fun `dot product of parallel vectors`() {
        val a = Vector2(2f, 0f)
        val b = Vector2(3f, 0f)
        assertEquals(6f, a.dot(b), epsilon)
    }

    @Test
    fun `distanceTo is correct`() {
        val a = Vector2(0f, 0f)
        val b = Vector2(3f, 4f)
        assertEquals(5f, a.distanceTo(b), epsilon)
    }

    @Test
    fun `clamp constrains values`() {
        val v = Vector2(-1f, 2f)
        val clamped = v.clamp(0f, 0f, 1f, 1f)
        assertEquals(0f, clamped.x, epsilon)
        assertEquals(1f, clamped.y, epsilon)
    }

    @Test
    fun `clamp passes through in-range values`() {
        val v = Vector2(0.5f, 0.5f)
        val clamped = v.clamp(0f, 0f, 1f, 1f)
        assertEquals(0.5f, clamped.x, epsilon)
        assertEquals(0.5f, clamped.y, epsilon)
    }

    @Test
    fun `lerp at 0 returns start`() {
        val a = Vector2(0f, 0f)
        val b = Vector2(10f, 10f)
        val result = a.lerp(b, 0f)
        assertEquals(0f, result.x, epsilon)
        assertEquals(0f, result.y, epsilon)
    }

    @Test
    fun `lerp at 1 returns target`() {
        val a = Vector2(0f, 0f)
        val b = Vector2(10f, 10f)
        val result = a.lerp(b, 1f)
        assertEquals(10f, result.x, epsilon)
        assertEquals(10f, result.y, epsilon)
    }

    @Test
    fun `lerp at 0_5 returns midpoint`() {
        val a = Vector2(0f, 0f)
        val b = Vector2(10f, 10f)
        val result = a.lerp(b, 0.5f)
        assertEquals(5f, result.x, epsilon)
        assertEquals(5f, result.y, epsilon)
    }

    @Test
    fun `lerp clamps t above 1`() {
        val a = Vector2(0f, 0f)
        val b = Vector2(10f, 10f)
        val result = a.lerp(b, 2f)
        assertEquals(10f, result.x, epsilon)
        assertEquals(10f, result.y, epsilon)
    }
}
