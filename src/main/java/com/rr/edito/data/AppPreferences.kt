package com.rr.edito.data

data class AppPreferences(
    val apiKey: String = "",
    val model: String = "gemini-2.5-flash-lite",
    val isServiceEnabled: Boolean = false
)

