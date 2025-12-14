package com.rr.edito.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "edito_preferences")

class PreferencesRepository(private val context: Context) {
    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val MODEL = stringPreferencesKey("model")
        private val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { preferences ->
        AppPreferences(
            apiKey = preferences[API_KEY] ?: "",
            model = preferences[MODEL] ?: "gemini-2.5-flash-lite",
            isServiceEnabled = preferences[SERVICE_ENABLED] ?: false
        )
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun saveModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[MODEL] = model
        }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_ENABLED] = enabled
        }
    }

    suspend fun getApiKey(): String {
        return preferences.map { it.apiKey }.first()
    }

    suspend fun getModel(): String {
        return preferences.map { it.model }.first()
    }
}

