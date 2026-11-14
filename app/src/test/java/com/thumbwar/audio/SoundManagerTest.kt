package com.thumbwar.audio

import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class SoundManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val vibrator: Vibrator =
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    private val sound = SoundManager(context)

    // Robolectric's vibrator, like many budget phones, reports no built-in effects, so these
    // check the fallback buzzes. The built-in effects need a phone that has them.
    @Test
    fun `without built-in effects, short moments keep their short buzzes`() {
        sound.vibrate(GameHaptic.PIN_TICK)
        assertEquals(0, shadowOf(vibrator).effectId)
        assertEquals(15L, shadowOf(vibrator).milliseconds)
        sound.vibrate(GameHaptic.PIN_START)
        assertEquals(100L, shadowOf(vibrator).milliseconds)
    }

    @Test
    fun `winning is a longer buzz`() {
        sound.vibrate(GameHaptic.ROUND_WON)
        assertTrue(shadowOf(vibrator).isVibrating)
        assertEquals(150L, shadowOf(vibrator).milliseconds)
    }

    @Test
    fun `vibrate does nothing when vibration is disabled`() {
        sound.setVibrationEnabled(false)
        GameHaptic.entries.forEach { sound.vibrate(it) }
        assertFalse(shadowOf(vibrator).isVibrating)
        assertEquals(0, shadowOf(vibrator).effectId)
    }

    @Test
    fun `every haptic plays without throwing`() {
        GameHaptic.entries.forEach { sound.vibrate(it) }
    }

    @Test
    fun `every game sound plays without throwing, enabled or not`() {
        GameSound.entries.forEach { sound.play(it) }
        sound.setSoundEnabled(false)
        GameSound.entries.forEach { sound.play(it) }
        sound.release()
    }
}
