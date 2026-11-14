package com.thumbwar.audio

import android.os.VibrationEffect

/**
 * What the phone does in the hand for each game moment. On phones that report them (Android 11+),
 * the short ones use the phone's own tuned effects: crisp clicks and ticks instead of a motor
 * buzz. [fallback] is a waveform (off/on timings in ms) for every other phone.
 *
 * @param effectId a VibrationEffect.EFFECT_* id, or null to always use [fallback]
 */
enum class GameHaptic(val effectId: Int?, val fallback: LongArray) {
    COUNTDOWN_BEAT(VibrationEffect.EFFECT_TICK, longArrayOf(0, 30)),
    COUNTDOWN_DECLARE(VibrationEffect.EFFECT_CLICK, longArrayOf(0, 50)),
    PIN_START(VibrationEffect.EFFECT_HEAVY_CLICK, longArrayOf(0, 100)),
    PIN_TICK(VibrationEffect.EFFECT_TICK, longArrayOf(0, 15)),
    PIN_ESCAPE(VibrationEffect.EFFECT_DOUBLE_CLICK, longArrayOf(0, 30, 40, 30)),

    // Winning stays a longer buzz: it should feel like an ending, not a click
    ROUND_WON(null, longArrayOf(0, 150)),
    MATCH_WON(null, longArrayOf(0, 100, 50, 100, 50, 200))
}
