package com.thumbwar.ui.screens.game

import org.junit.Assert.assertEquals
import org.junit.Test

class FrameTimerTest {

    private fun steps(frameNanos: List<Long>): List<Long> {
        val timer = FrameTimer()
        return frameNanos.map { timer.stepMs(it) }
    }

    @Test
    fun `first frame only starts the clock`() {
        assertEquals(listOf(0L, 16L), steps(listOf(5_000_000_000L, 5_016_000_000L)))
    }

    @Test
    fun `at 90 Hz game time keeps up with real time`() {
        val frameNanos = 1_000_000_000L / 90
        val frames = List(91) { it * frameNanos } // one second of frames
        val played = steps(frames).sum()
        // Rounding each 11.1 ms frame down to 11 ms would lose 10 ms a second
        assertEquals(999L, played) // within a millisecond of the 1000 ms that passed
    }

    @Test
    fun `at 120 Hz frames alternate 8 and 9 ms`() {
        val frameNanos = 1_000_000_000L / 120
        val played = steps(List(121) { it * frameNanos })
        assertEquals(setOf(0L, 8L, 9L), played.toSet())
        assertEquals(999L, played.sum())
    }

    @Test
    fun `a stall is capped instead of jumping the game forward`() {
        val played = steps(listOf(0L, 16_000_000L, 2_016_000_000L, 2_032_000_000L))
        assertEquals(listOf(0L, 16L, FrameTimer.MAX_FRAME_MS, 16L), played)
    }

    @Test
    fun `a repeated frame time plays nothing`() {
        assertEquals(listOf(0L, 16L, 0L), steps(listOf(0L, 16_000_000L, 16_000_000L)))
    }
}
