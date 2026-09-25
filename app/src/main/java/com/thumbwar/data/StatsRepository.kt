package com.thumbwar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.statsStore: DataStore<Preferences> by preferencesDataStore(name = "stats")

class StatsRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.statsStore)

    companion object {
        private val WINS = intPreferencesKey("wins")
        private val LOSSES = intPreferencesKey("losses")
        private val CURRENT_STREAK = intPreferencesKey("current_streak")
        private val BEST_STREAK = intPreferencesKey("best_streak")
    }

    val wins: Flow<Int> = dataStore.data.map { it[WINS] ?: 0 }
    val losses: Flow<Int> = dataStore.data.map { it[LOSSES] ?: 0 }
    val currentStreak: Flow<Int> = dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val bestStreak: Flow<Int> = dataStore.data.map { it[BEST_STREAK] ?: 0 }

    suspend fun recordWin() {
        dataStore.edit { prefs ->
            val wins = (prefs[WINS] ?: 0) + 1
            val streak = (prefs[CURRENT_STREAK] ?: 0) + 1
            val best = prefs[BEST_STREAK] ?: 0
            prefs[WINS] = wins
            prefs[CURRENT_STREAK] = streak
            if (streak > best) prefs[BEST_STREAK] = streak
        }
    }

    suspend fun recordLoss() {
        dataStore.edit { prefs ->
            prefs[LOSSES] = (prefs[LOSSES] ?: 0) + 1
            prefs[CURRENT_STREAK] = 0
        }
    }

    suspend fun resetStats() {
        dataStore.edit { prefs ->
            prefs[WINS] = 0
            prefs[LOSSES] = 0
            prefs[CURRENT_STREAK] = 0
            prefs[BEST_STREAK] = 0
        }
    }
}
