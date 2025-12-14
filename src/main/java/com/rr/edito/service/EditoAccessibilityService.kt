package com.rr.edito.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.LruCache
import android.os.Build
import com.rr.edito.data.PreferencesRepository
import com.rr.edito.data.PrePromptRepository
import com.rr.edito.data.PrePrompt
import com.rr.edito.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditoAccessibilityService : AccessibilityService() {
    private var lastText: String = ""
    private val cache = LruCache<String, String>(15)
    private lateinit var prePromptRepository: PrePromptRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private var apiKey: String = ""
    private var model: String = "gemini-2.5-flash-lite"
    private var prePrompts: List<PrePrompt> = PrePromptRepository.defaultPrePrompts

    override fun onServiceConnected() {
        super.onServiceConnected()
        prePromptRepository = PrePromptRepository(this)
        preferencesRepository = PreferencesRepository(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiKey = preferencesRepository.getApiKey()
                model = preferencesRepository.getModel()
                prePrompts = prePromptRepository.prePrompts.first()
                android.util.Log.d("EditoService", "Preferences loaded: API key=${if(apiKey.isNotEmpty()) "set" else "empty"}, Model=$model, PrePrompts=${prePrompts.size}")
                
                // If prePrompts are empty, use defaults
                if (prePrompts.isEmpty()) {
                    prePrompts = PrePromptRepository.defaultPrePrompts
                    android.util.Log.d("EditoService", "Using default prePrompts: ${prePrompts.size}")
                }
            } catch (e: Exception) {
                android.util.Log.e("EditoService", "Error loading preferences", e)
                e.printStackTrace()
                // Use defaults if loading fails
                prePrompts = PrePromptRepository.defaultPrePrompts
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Listen to text change events
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            handleTextChanged(event)
        }
    }
    
    private fun handleTextChanged(event: AccessibilityEvent) {
        try {
            // Get text from the event source
            val source = event.source ?: return
            val text = getTextFromEvent(event) ?: getTextFromNode(source)
            
            if (text.isNullOrEmpty() || text == lastText) {
                source.recycle()
                return
            }
            
            lastText = text
            
            // Check if text contains a trigger keyword
            val trigger = findTrigger(text) ?: run {
                source.recycle()
                return
            }
            
            // Small delay to ensure trigger is fully typed
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // Get fresh node reference
                    val freshSource = rootInActiveWindow
                    val currentText = getTextFromNode(freshSource)
                    
                    if (currentText != null && findTrigger(currentText) == trigger) {
                        // Check cache first
                        val cacheKey = "$currentText|$trigger"
                        val cached = cache.get(cacheKey)
                        if (cached != null) {
                            replaceText(freshSource, cached)
                            freshSource?.recycle()
                            return@postDelayed
                        }
                        
                        // Process with AI
                        processText(currentText, trigger)
                    }
                    freshSource?.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 300) // Wait 300ms after trigger is detected
            
            source.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun getTextFromEvent(event: AccessibilityEvent): String? {
        val text = event.text?.toString() ?: ""
        if (text.isNotEmpty()) {
            return text
        }
        
        // Try getting text from before/after text
        val beforeText = event.beforeText?.toString() ?: ""
        val afterText = event.text?.toString() ?: ""
        return afterText.ifEmpty { beforeText }
    }

    private fun findTrigger(text: String): String? {
        if (prePrompts.isEmpty()) {
            android.util.Log.w("EditoService", "No prePrompts loaded yet")
            return null
        }
        
        val trimmed = text.trim()
        for (prePrompt in prePrompts) {
            if (trimmed.endsWith(prePrompt.keyword, ignoreCase = true)) {
                android.util.Log.d("EditoService", "Found trigger: ${prePrompt.keyword} in text: $trimmed")
                return prePrompt.keyword
            }
        }
        return null
    }

    private fun processText(text: String, trigger: String) {
        val prePrompt = prePrompts.find { it.keyword == trigger } ?: return
        val userText = text.substringBefore(trigger).trim()

        if (apiKey.isEmpty()) {
            android.util.Log.e("EditoService", "API key is empty!")
            return
        }

        if (userText.isEmpty()) {
            android.util.Log.e("EditoService", "User text is empty!")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prompt = buildPrompt(userText, prePrompt.instruction)
                android.util.Log.d("EditoService", "Processing: $userText with trigger: $trigger")
                
                val api = GeminiService.create(apiKey, model)
                val url = GeminiService.getApiUrl(model, apiKey)
                
                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = prompt))
                        )
                    )
                )

                val response = api.generateContent(url, request)
                
                if (response.isSuccessful) {
                    val result = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!result.isNullOrEmpty()) {
                        android.util.Log.d("EditoService", "Got result: $result")
                        withContext(Dispatchers.Main) {
                            // Cache the result
                            val cacheKey = "$text|$trigger"
                            cache.put(cacheKey, result)
                            
                            // Get fresh node reference for replacement
                            val freshNode = rootInActiveWindow
                            if (freshNode != null) {
                                replaceText(freshNode, result)
                                freshNode.recycle()
                            }
                        }
                    } else {
                        android.util.Log.e("EditoService", "Empty result from API")
                    }
                } else {
                    android.util.Log.e("EditoService", "API call failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("EditoService", "Error processing text", e)
                e.printStackTrace()
            }
        }
    }

    private fun buildPrompt(userText: String, instruction: String): String {
        return if (instruction.contains("answer", ignoreCase = true)) {
            userText
        } else {
            "$instruction: $userText"
        }
    }

    private fun getTextFromNode(nodeInfo: AccessibilityNodeInfo?): String? {
        if (nodeInfo == null) return null

        // Try to get text from editable nodes first
        if (nodeInfo.isEditable) {
            val text = nodeInfo.text?.toString()
            if (!text.isNullOrEmpty()) {
                return text
            }
        }

        // Try to get text directly
        val text = nodeInfo.text?.toString()
        if (!text.isNullOrEmpty()) {
            return text
        }

        // Try to get from child nodes (for complex layouts like WhatsApp)
        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(i)
            if (child != null) {
                val childText = getTextFromNode(child)
                if (!childText.isNullOrEmpty()) {
                    return childText
                }
            }
        }

        return null
    }

    private fun replaceText(nodeInfo: AccessibilityNodeInfo, newText: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val editable = findEditableNode(nodeInfo) ?: return
                
                // Get a fresh reference to avoid recycling issues
                val freshEditable = findEditableNode(rootInActiveWindow) ?: editable
                
                // Focus the field first
                freshEditable.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                
                // Wait a bit for focus then replace text
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        val editableNode = findEditableNode(rootInActiveWindow) ?: return@postDelayed
                        
                        // Select all text
                        val selectionBundle = android.os.Bundle().apply {
                            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0)
                            putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, editableNode.text?.length ?: 0)
                        }
                        editableNode.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectionBundle)
                        
                        // Use Clipboard to paste text (more reliable)
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("edito_text", newText)
                        clipboard.setPrimaryClip(clip)
                        
                        // Wait a bit then paste
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            try {
                                val pasteNode = findEditableNode(rootInActiveWindow) ?: return@postDelayed
                                pasteNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                                pasteNode.recycle()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, 100)
                        
                        editableNode.recycle()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 200)
                
                if (editable != freshEditable) {
                    editable.recycle()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun findEditableNode(nodeInfo: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (nodeInfo == null) return null

        if (nodeInfo.isEditable) {
            return nodeInfo
        }

        for (i in 0 until nodeInfo.childCount) {
            val child = nodeInfo.getChild(i)
            if (child != null) {
                val editable = findEditableNode(child)
                if (editable != null) {
                    return editable
                }
            }
        }

        return null
    }

    override fun onInterrupt() {
        // Service interrupted
    }
}

