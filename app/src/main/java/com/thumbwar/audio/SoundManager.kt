package com.thumbwar.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.VisibleForTesting

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

    // Short game sounds, decoded once up front. A sound asked for before its file has loaded is
    // skipped (they load in a few ms; the app shares one SoundManager, so only the very first
    // countdown beat after launch could miss).
    @VisibleForTesting
    internal val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val soundIds = mutableMapOf<GameSound, Int>()
    private val loadedIds = mutableSetOf<Int>()

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedIds += sampleId
        }
        GameSound.entries.forEach { soundIds[it] = soundPool.load(context, it.resId, 1) }
    }

    fun play(sound: GameSound) {
        if (!soundEnabled) return
        val id = soundIds[sound] ?: return
        if (id !in loadedIds) return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
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
        soundPool.release()
    }

    companion object {
        // Enough for a pin tick to overlap the thud or a jingle without cutting either off
        private const val MAX_STREAMS = 4

        @Volatile private var shared: SoundManager? = null

        /** The app's one SoundManager, so sounds are decoded once rather than for every match. */
        fun shared(context: Context): SoundManager =
            shared ?: synchronized(this) {
                shared ?: SoundManager(context.applicationContext).also { shared = it }
            }

        /** Robolectric gives every test a fresh app; forget the previous test's SoundManager. */
        @VisibleForTesting
        internal fun resetShared() {
            shared?.release()
            shared = null
        }
    }
}
