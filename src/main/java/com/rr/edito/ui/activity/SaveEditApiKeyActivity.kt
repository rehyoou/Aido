package com.rr.edito.ui.activity

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rr.edito.R
import com.rr.edito.data.PreferencesRepository
import com.rr.edito.network.GeminiApi
import com.rr.edito.network.GeminiRequest
import com.rr.edito.network.GeminiService
import com.rr.edito.network.Content
import com.rr.edito.network.Part
import kotlinx.coroutines.launch

class SaveEditApiKeyActivity : AppCompatActivity() {
    private lateinit var apiKeyEditText: EditText
    private lateinit var modelSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var toggleVisibilityButton: ImageButton
    private lateinit var preferencesRepository: PreferencesRepository
    private var isPasswordVisible = false

    private val models = listOf(
        "gemini-2.5-flash-lite",
        "gemini-2.5-flash",
        "gemini-2.5-pro",
        "gemma-3n-e2b-it",
        "gemma-3n-e4b-it"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_edit_api_key)

        preferencesRepository = PreferencesRepository(this)
        
        apiKeyEditText = findViewById(R.id.apiKeyEditText)
        modelSpinner = findViewById(R.id.modelSpinner)
        saveButton = findViewById(R.id.saveButton)
        toggleVisibilityButton = findViewById(R.id.toggleVisibilityButton)

        setupViews()
        loadCurrentSettings()
    }

    private fun setupViews() {
        toggleVisibilityButton.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            apiKeyEditText.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            apiKeyEditText.setSelection(apiKeyEditText.text.length)
        }

        saveButton.setOnClickListener {
            val apiKey = apiKeyEditText.text.toString().trim()
            val selectedModel = models[modelSpinner.selectedItemPosition]

            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Please enter API key", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    // Test API key first
                    val success = testApiKey(apiKey, selectedModel)
                    if (success) {
                        preferencesRepository.saveApiKey(apiKey)
                        preferencesRepository.saveModel(selectedModel)
                        runOnUiThread {
                            Toast.makeText(this@SaveEditApiKeyActivity, "API key saved successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@SaveEditApiKeyActivity, "Invalid API key", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@SaveEditApiKeyActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Setup spinner adapter
        val spinnerAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            models
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = spinnerAdapter
    }

    private fun loadCurrentSettings() {
        lifecycleScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                apiKeyEditText.setText(prefs.apiKey)
                val modelIndex = models.indexOf(prefs.model)
                if (modelIndex >= 0) {
                    modelSpinner.setSelection(modelIndex)
                }
            }
        }
    }

    private suspend fun testApiKey(apiKey: String, model: String): Boolean {
        return try {
            val api = GeminiService.create(apiKey, model)
            val url = GeminiService.getApiUrl(model, apiKey)
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = "test"))
                    )
                )
            )

            val response = api.generateContent(url, request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

