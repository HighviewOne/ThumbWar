package com.thumbwar.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.settingsStore)

    companion object {
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")
    }

    val soundEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED] ?: true
    }

    val defaultDifficulty: Flow<String> = dataStore.data.map { prefs ->
        prefs[DEFAULT_DIFFICULTY] ?: "MEDIUM"
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setDefaultDifficulty(difficulty: String) {
        dataStore.edit { prefs ->
            prefs[DEFAULT_DIFFICULTY] = difficulty
        }
    }
}
