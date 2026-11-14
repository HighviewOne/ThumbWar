package com.thumbwar.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.thumbwar.ai.AiDifficulty
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

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
        repo.recordWin(AiDifficulty.HARD)
        repo.recordWin(AiDifficulty.HARD)
        assertEquals(2, repo.wins.first())
        assertEquals(2, repo.currentStreak.first())
        assertEquals(2, repo.bestStreak.first())
    }

    @Test
    fun `recordLoss increments losses and resets current streak only`() = runTest {
        repo.recordWin(AiDifficulty.HARD)
        repo.recordWin(AiDifficulty.HARD)
        repo.recordLoss(AiDifficulty.HARD)
        assertEquals(1, repo.losses.first())
        assertEquals(0, repo.currentStreak.first())
        assertEquals(2, repo.bestStreak.first())
    }

    @Test
    fun `best streak only grows when current streak beats it`() = runTest {
        repo.recordWin(AiDifficulty.HARD)
        repo.recordWin(AiDifficulty.HARD)
        repo.recordWin(AiDifficulty.HARD)
        repo.recordLoss(AiDifficulty.HARD)
        repo.recordWin(AiDifficulty.HARD)
        assertEquals(1, repo.currentStreak.first())
        assertEquals(3, repo.bestStreak.first())
    }

    @Test
    fun `resetStats clears everything`() = runTest {
        repo.recordWin(AiDifficulty.HARD)
        repo.recordLoss(AiDifficulty.HARD)
        repo.recordPin(AiDifficulty.HARD, 3000)
        repo.resetStats()
        assertEquals(0, repo.wins.first())
        assertEquals(0, repo.losses.first())
        assertEquals(0, repo.currentStreak.first())
        assertEquals(0, repo.bestStreak.first())
        assertEquals(DifficultyStats(), repo.byDifficulty.first()[AiDifficulty.HARD])
    }

    @Test
    fun `each difficulty keeps its own wins and losses, and totals add them up`() = runTest {
        repo.recordWin(AiDifficulty.EASY)
        repo.recordWin(AiDifficulty.EASY)
        repo.recordWin(AiDifficulty.HARD)
        repo.recordLoss(AiDifficulty.MEDIUM)
        val by = repo.byDifficulty.first()
        assertEquals(DifficultyStats(wins = 2), by[AiDifficulty.EASY])
        assertEquals(DifficultyStats(losses = 1), by[AiDifficulty.MEDIUM])
        assertEquals(DifficultyStats(wins = 1), by[AiDifficulty.HARD])
        assertEquals(3, repo.wins.first())
        assertEquals(1, repo.losses.first())
    }

    @Test
    fun `a pin is a record only when it beats that difficulty's fastest`() = runTest {
        assertTrue("First pin is a record", repo.recordPin(AiDifficulty.HARD, 4000))
        assertFalse("Slower", repo.recordPin(AiDifficulty.HARD, 4500))
        assertFalse("Equal", repo.recordPin(AiDifficulty.HARD, 4000))
        assertTrue("Faster", repo.recordPin(AiDifficulty.HARD, 3200))
        assertTrue("Other difficulties keep their own", repo.recordPin(AiDifficulty.EASY, 9000))
        val by = repo.byDifficulty.first()
        assertEquals(3200L, by[AiDifficulty.HARD]?.fastestPinMs)
        assertEquals(9000L, by[AiDifficulty.EASY]?.fastestPinMs)
        assertNull(by[AiDifficulty.MEDIUM]?.fastestPinMs)
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
