package com.thumbwar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED] ?: true
    }

    val defaultDifficulty: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_DIFFICULTY] ?: "MEDIUM"
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setDefaultDifficulty(difficulty: String) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_DIFFICULTY] = difficulty
        }
    }
}
