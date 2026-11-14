package com.thumbwar.ui.screens.game

import androidx.compose.runtime.MonotonicFrameClock
import com.thumbwar.engine.GameConfig
import kotlinx.coroutines.delay

/**
 * A 60 Hz display on virtual time: each frame waits [GameConfig.TICK_RATE_MS] with delay(), so
 * the test's scheduler (or looper) decides when frames happen, and [nowMs] gives the frame time.
 */
class FakeFrameClock(private val nowMs: () -> Long) : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        delay(GameConfig.TICK_RATE_MS)
        return onFrame(nowMs() * 1_000_000L)
    }
}
