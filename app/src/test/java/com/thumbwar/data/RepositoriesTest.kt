package com.thumbwar.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Real DataStore backed by a temp file, so repositories run on the plain JVM. */
abstract class DataStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    protected fun newDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) { File(tmp.newFolder(), "test.preferences_pb") }

    @After
    fun closeDataStore() {
        scope.cancel()
    }
}

class StatsRepositoryTest : DataStoreTest() {

    private val repo by lazy { StatsRepository(newDataStore()) }

    @Test
    fun `stats start at zero`() = runTest {
        assertEquals(0, repo.wins.first())
        assertEquals(0, repo.losses.first())
        assertEquals(0, repo.currentStreak.first())
        assertEquals(0, repo.bestStreak.first())
    }

    @Test
    fun `recordWin increments wins and streaks`() = runTest {
        repo.recordWin()
        repo.recordWin()
        assertEquals(2, repo.wins.first())
        assertEquals(2, repo.currentStreak.first())
        assertEquals(2, repo.bestStreak.first())
    }

    @Test
    fun `recordLoss increments losses and resets current streak only`() = runTest {
        repo.recordWin()
        repo.recordWin()
        repo.recordLoss()
        assertEquals(1, repo.losses.first())
        assertEquals(0, repo.currentStreak.first())
        assertEquals(2, repo.bestStreak.first())
    }

    @Test
    fun `best streak only grows when current streak beats it`() = runTest {
        repo.recordWin()
        repo.recordWin()
        repo.recordWin()
        repo.recordLoss()
        repo.recordWin()
        assertEquals(1, repo.currentStreak.first())
        assertEquals(3, repo.bestStreak.first())
    }

    @Test
    fun `resetStats clears everything`() = runTest {
        repo.recordWin()
        repo.recordLoss()
        repo.resetStats()
        assertEquals(0, repo.wins.first())
        assertEquals(0, repo.losses.first())
        assertEquals(0, repo.currentStreak.first())
        assertEquals(0, repo.bestStreak.first())
    }
}

class PreferencesRepositoryTest : DataStoreTest() {

    private val repo by lazy { PreferencesRepository(newDataStore()) }

    @Test
    fun `defaults are sound on, vibration on, medium difficulty`() = runTest {
        assertTrue(repo.soundEnabled.first())
        assertTrue(repo.vibrationEnabled.first())
        assertEquals("MEDIUM", repo.defaultDifficulty.first())
    }

    @Test
    fun `settings persist`() = runTest {
        repo.setSoundEnabled(false)
        repo.setVibrationEnabled(false)
        repo.setDefaultDifficulty("HARD")
        assertFalse(repo.soundEnabled.first())
        assertFalse(repo.vibrationEnabled.first())
        assertEquals("HARD", repo.defaultDifficulty.first())
    }

    @Test
    fun `settings are independent`() = runTest {
        repo.setSoundEnabled(false)
        assertTrue(repo.vibrationEnabled.first())
    }
}
