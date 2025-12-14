package com.rr.edito.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.rr.edito.R

class ChatActivity : AppCompatActivity() {
    private lateinit var inputEditText: EditText
    private lateinit var outputTextView: TextView
    private lateinit var demoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        inputEditText = findViewById(R.id.inputEditText)
        outputTextView = findViewById(R.id.outputTextView)
        demoButton = findViewById(R.id.demoButton)

        setupViews()
    }

    private fun setupViews() {
        demoButton?.setOnClickListener {
            // Demo functionality
            val demoText = "i cant belive hw awsome ths app is! @fixg"
            inputEditText.setText(demoText)
        }
    }
}

