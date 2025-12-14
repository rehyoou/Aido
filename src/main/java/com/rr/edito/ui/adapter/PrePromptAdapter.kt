package com.rr.edito.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rr.edito.R
import com.rr.edito.data.PrePrompt
import com.rr.edito.data.PrePromptRepository

class PrePromptAdapter(
    private val repository: PrePromptRepository,
    private val onItemClick: (PrePrompt) -> Unit
) : ListAdapter<PrePrompt, PrePromptAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val keywordText: TextView = view.findViewById(R.id.keywordText)
        val instructionText: TextView = view.findViewById(R.id.instructionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preprompt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.keywordText.text = item.keyword
        holder.instructionText.text = item.instruction
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PrePrompt>() {
        override fun areItemsTheSame(oldItem: PrePrompt, newItem: PrePrompt): Boolean {
            return oldItem.keyword == newItem.keyword
        }

        override fun areContentsTheSame(oldItem: PrePrompt, newItem: PrePrompt): Boolean {
            return oldItem == newItem
        }
    }
}

