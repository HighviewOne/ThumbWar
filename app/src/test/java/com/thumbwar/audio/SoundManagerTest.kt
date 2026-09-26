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

    @Test
    fun `vibrate buzzes for the requested duration`() {
        sound.vibrate(120)
        assertTrue(shadowOf(vibrator).isVibrating)
        assertEquals(120L, shadowOf(vibrator).milliseconds)
    }

    @Test
    fun `vibrate does nothing when vibration is disabled`() {
        sound.setVibrationEnabled(false)
        sound.vibrate(120)
        sound.vibratePattern(longArrayOf(0, 100, 50, 100))
        assertFalse(shadowOf(vibrator).isVibrating)
    }

    @Test
    fun `vibratePattern buzzes when enabled`() {
        sound.vibratePattern(longArrayOf(0, 100, 50, 100))
        assertTrue(shadowOf(vibrator).isVibrating)
    }

    @Test
    fun `every game sound plays without throwing, enabled or not`() {
        GameSound.entries.forEach { sound.play(it) }
        sound.setSoundEnabled(false)
        GameSound.entries.forEach { sound.play(it) }
        sound.release()
    }
}
