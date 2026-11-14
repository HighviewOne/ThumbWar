package com.thumbwar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.thumbwar.ai.AiDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.statsStore: DataStore<Preferences> by preferencesDataStore(name = "stats")

/** Record against the computer on one difficulty. */
data class DifficultyStats(
    val wins: Int = 0,
    val losses: Int = 0,
    /** Quickest winning pin, from "go" to the pin completing; null until the first one. */
    val fastestPinMs: Long? = null
)

/**
 * Stats for matches against the computer. Totals and streaks cover every difficulty; each
 * difficulty also keeps its own wins, losses and fastest pin. Results from before per-difficulty
 * stats existed never recorded their difficulty, so they only count in the totals.
 */
class StatsRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.statsStore)

    companion object {
        private val WINS = intPreferencesKey("wins")
        private val LOSSES = intPreferencesKey("losses")
        private val CURRENT_STREAK = intPreferencesKey("current_streak")
        private val BEST_STREAK = intPreferencesKey("best_streak")

        private fun winsKey(d: AiDifficulty) = intPreferencesKey("wins_${d.name.lowercase()}")
        private fun lossesKey(d: AiDifficulty) = intPreferencesKey("losses_${d.name.lowercase()}")
        private fun fastestPinKey(d: AiDifficulty) = longPreferencesKey("fastest_pin_ms_${d.name.lowercase()}")
    }

    val wins: Flow<Int> = dataStore.data.map { it[WINS] ?: 0 }
    val losses: Flow<Int> = dataStore.data.map { it[LOSSES] ?: 0 }
    val currentStreak: Flow<Int> = dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val bestStreak: Flow<Int> = dataStore.data.map { it[BEST_STREAK] ?: 0 }

    /** Every difficulty's record, in [AiDifficulty] order. */
    val byDifficulty: Flow<Map<AiDifficulty, DifficultyStats>> = dataStore.data.map { prefs ->
        AiDifficulty.entries.associateWith { d ->
            DifficultyStats(
                wins = prefs[winsKey(d)] ?: 0,
                losses = prefs[lossesKey(d)] ?: 0,
                fastestPinMs = prefs[fastestPinKey(d)]
            )
        }
    }

    suspend fun recordWin(difficulty: AiDifficulty) {
        dataStore.edit { prefs ->
            val streak = (prefs[CURRENT_STREAK] ?: 0) + 1
            prefs[WINS] = (prefs[WINS] ?: 0) + 1
            prefs[winsKey(difficulty)] = (prefs[winsKey(difficulty)] ?: 0) + 1
            prefs[CURRENT_STREAK] = streak
            if (streak > (prefs[BEST_STREAK] ?: 0)) prefs[BEST_STREAK] = streak
        }
    }

    suspend fun recordLoss(difficulty: AiDifficulty) {
        dataStore.edit { prefs ->
            prefs[LOSSES] = (prefs[LOSSES] ?: 0) + 1
            prefs[lossesKey(difficulty)] = (prefs[lossesKey(difficulty)] ?: 0) + 1
            prefs[CURRENT_STREAK] = 0
        }
    }

    /** Saves a winning pin's time; returns true if it beat the difficulty's fastest (or was the first). */
    suspend fun recordPin(difficulty: AiDifficulty, pinMs: Long): Boolean {
        var isRecord = false
        dataStore.edit { prefs ->
            val best = prefs[fastestPinKey(difficulty)]
            isRecord = best == null || pinMs < best
            if (isRecord) prefs[fastestPinKey(difficulty)] = pinMs
        }
        return isRecord
    }

    suspend fun resetStats() {
        dataStore.edit { it.clear() }
    }
}
