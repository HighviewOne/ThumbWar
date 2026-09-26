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

    // Using ToneGenerator for simple sound effects without requiring audio assets
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

    fun vibrate(durationMs: Long = 50) {
        if (!vibrationEnabled) return
        try {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    fun vibratePattern(pattern: LongArray) {
        if (!vibrationEnabled) return
        try {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (_: Exception) {}
    }

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
