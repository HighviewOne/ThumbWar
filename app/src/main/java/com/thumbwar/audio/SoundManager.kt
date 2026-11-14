package com.thumbwar.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundManager(private val context: Context) {

    private var soundEnabled = true
    private var vibrationEnabled = true

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Using ToneGenerator for simple sound effects without requiring audio assets.
    // Some devices throw from its constructor (no audio resources); the game then plays silently.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private val toneGenerator: ToneGenerator? by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            null
        }
    }

    fun play(sound: GameSound) {
        if (!soundEnabled) return

        // Map game sounds to tone types for simple audio feedback
        val toneType = when (sound) {
            GameSound.COUNTDOWN_BEAT -> ToneGenerator.TONE_PROP_BEEP
            GameSound.COUNTDOWN_DECLARE -> ToneGenerator.TONE_PROP_BEEP2
            GameSound.COLLISION_THUD -> ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
            GameSound.PIN_TICK -> ToneGenerator.TONE_DTMF_9
            GameSound.PIN_ESCAPE -> ToneGenerator.TONE_PROP_PROMPT
            GameSound.PIN_COMPLETE -> ToneGenerator.TONE_PROP_ACK
            GameSound.VICTORY_FANFARE -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            GameSound.BUTTON_TAP -> ToneGenerator.TONE_PROP_BEEP
        }

        // Pin ticks come as fast as every 120 ms, so they must end before the next one starts
        val durationMs = if (sound == GameSound.PIN_TICK) 50 else 150

        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (_: Exception) {}
    }

    fun vibrate(haptic: GameHaptic) {
        if (!vibrationEnabled) return
        try {
            vibrator.vibrate(effectFor(haptic))
        } catch (_: Exception) {}
    }

    private fun effectFor(haptic: GameHaptic): VibrationEffect {
        val id = haptic.effectId
        return if (id != null && hasBuiltInEffect(id)) {
            VibrationEffect.createPredefined(id)
        } else if (haptic.fallback.size == 2) {
            // A single buzz, as before (DEFAULT_AMPLITUDE: many phones can't vary strength anyway)
            VibrationEffect.createOneShot(haptic.fallback[1], VibrationEffect.DEFAULT_AMPLITUDE)
        } else {
            VibrationEffect.createWaveform(haptic.fallback, -1)
        }
    }

    // Only when the phone says yes outright (Android 11+). When it says "unknown", Android swaps in
    // a generic buzz of its own, so those phones keep the game's own buzzes instead.
    private fun hasBuiltInEffect(effectId: Int): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator.areAllEffectsSupported(effectId) == Vibrator.VIBRATION_EFFECT_SUPPORT_YES

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        vibrationEnabled = enabled
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (_: Exception) {}
    }
}
