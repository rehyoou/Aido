package com.rr.edito.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rr.edito.R
import androidx.lifecycle.lifecycleScope
import com.rr.edito.data.PrePromptRepository
import com.rr.edito.ui.adapter.PrePromptAdapter
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var prePromptRepository: PrePromptRepository
    private lateinit var adapter: PrePromptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prePromptRepository = PrePromptRepository(this)
        
        recyclerView = findViewById(R.id.prePromptsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = PrePromptAdapter(prePromptRepository) { prePrompt ->
            // Handle edit/delete
        }
        recyclerView.adapter = adapter

        loadPrePrompts()
    }

    private fun loadPrePrompts() {
        lifecycleScope.launch {
            prePromptRepository.prePrompts.collect { prePrompts ->
                adapter.submitList(prePrompts)
            }
        }
    }
}

