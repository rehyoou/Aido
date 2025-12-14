package com.rr.edito.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.prePromptDataStore: DataStore<Preferences> by preferencesDataStore(name = "edito_preprompts")

class PrePromptRepository(private val context: Context) {
    private val gson = Gson()
    
    companion object {
        private val PREPROMPTS_KEY = stringPreferencesKey("preprompts")
        
        val defaultPrePrompts = listOf(
            PrePrompt("@edito", "Get complete, relevant answers"),
            PrePrompt("@fixg", "Fix grammar, spelling, and punctuation"),
            PrePrompt("@summ", "Summarize text in 1-2 sentences"),
            PrePrompt("@polite", "Rewrite in professional tone"),
            PrePrompt("@casual", "Rewrite in friendly, casual tone"),
            PrePrompt("@expand", "Add more detail and elaboration"),
            PrePrompt("@bullet", "Convert to clear bullet points"),
            PrePrompt("@improve", "Enhance writing quality and clarity"),
            PrePrompt("@rephrase", "Say the same thing differently"),
            PrePrompt("@emoji", "Add relevant emojis to text"),
            PrePrompt("@formal", "Rewrite in formal business tone"),
            PrePrompt("@funny", "Add humor to make it funny")
        )
    }

    val prePrompts: Flow<List<PrePrompt>> = context.prePromptDataStore.data.map { preferences ->
        val json = preferences[PREPROMPTS_KEY]
        if (json.isNullOrEmpty()) {
            defaultPrePrompts
        } else {
            try {
                val type = object : TypeToken<List<PrePrompt>>() {}.type
                gson.fromJson<List<PrePrompt>>(json, type) ?: defaultPrePrompts
            } catch (e: Exception) {
                defaultPrePrompts
            }
        }
    }

    suspend fun savePrePrompts(prePrompts: List<PrePrompt>) {
        context.prePromptDataStore.edit { preferences ->
            val json = gson.toJson(prePrompts)
            preferences[PREPROMPTS_KEY] = json
        }
    }

    suspend fun addPrePrompt(prePrompt: PrePrompt) {
        val current = prePrompts.first()
        val updated = current + prePrompt
        savePrePrompts(updated)
    }

    suspend fun deletePrePrompt(keyword: String) {
        val current = prePrompts.first()
        val updated = current.filter { it.keyword != keyword }
        savePrePrompts(updated)
    }

    suspend fun updatePrePrompt(oldKeyword: String, newPrePrompt: PrePrompt) {
        val current = prePrompts.first()
        val updated = current.map { if (it.keyword == oldKeyword) newPrePrompt else it }
        savePrePrompts(updated)
    }
}


