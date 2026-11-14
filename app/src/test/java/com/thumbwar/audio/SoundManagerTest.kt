package com.thumbwar.audio

import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
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
    fun `each game sound plays its own file once it has loaded`() {
        val pool = shadowOf(sound.soundPool)
        GameSound.entries.forEach { pool.notifyResourceLoaded(it.resId, true) }
        GameSound.entries.forEach { sound.play(it) }
        GameSound.entries.forEach { assertTrue("$it should play", pool.wasResourcePlayed(it.resId)) }
        assertEquals("Every sound has its own file", GameSound.entries.size, GameSound.entries.map { it.resId }.toSet().size)
    }

    @Test
    fun `a sound asked for before it has loaded is skipped, not queued`() {
        val pool = shadowOf(sound.soundPool)
        sound.play(GameSound.PIN_TICK)
        pool.notifyResourceLoaded(GameSound.PIN_TICK.resId, true)
        assertFalse(pool.wasResourcePlayed(GameSound.PIN_TICK.resId))
    }

    @Test
    fun `nothing plays when sound is off`() {
        val pool = shadowOf(sound.soundPool)
        GameSound.entries.forEach { pool.notifyResourceLoaded(it.resId, true) }
        sound.setSoundEnabled(false)
        GameSound.entries.forEach { sound.play(it) }
        GameSound.entries.forEach { assertFalse(pool.wasResourcePlayed(it.resId)) }
        sound.release()
    }

    @Test
    fun `the app shares one sound manager`() {
        assertSame(SoundManager.shared(context), SoundManager.shared(context))
    }
}
