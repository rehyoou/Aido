package com.rr.edito.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rr.edito.R
import com.rr.edito.data.PreferencesRepository
import com.rr.edito.data.PrePromptRepository
import com.rr.edito.service.EditoAccessibilityService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var serviceSwitch: Switch
    private lateinit var serviceStatusText: TextView
    private lateinit var disableServiceButton: Button
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var prePromptRepository: PrePromptRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesRepository = PreferencesRepository(this)
        prePromptRepository = PrePromptRepository(this)

        serviceSwitch = findViewById(R.id.serviceSwitch)
        serviceStatusText = findViewById(R.id.serviceStatusText)
        disableServiceButton = findViewById(R.id.disableServiceButton)

        setupViews()
        checkServiceStatus()
    }

    private fun setupViews() {
        serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                openAccessibilitySettings()
            } else {
                lifecycleScope.launch {
                    preferencesRepository.setServiceEnabled(false)
                }
            }
        }

        disableServiceButton.setOnClickListener {
            lifecycleScope.launch {
                preferencesRepository.setServiceEnabled(false)
                serviceSwitch.isChecked = false
                Toast.makeText(this@MainActivity, "Service disabled", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.changeApiKeyButton)?.setOnClickListener {
            startActivity(Intent(this, SaveEditApiKeyActivity::class.java))
        }

        findViewById<View>(R.id.startChattingButton)?.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        findViewById<View>(R.id.mapPrePromptsButton)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<View>(R.id.viewOnboardingButton)?.setOnClickListener {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
    }

    private fun checkServiceStatus() {
        val isEnabled = isAccessibilityServiceEnabled()
        serviceSwitch.isChecked = isEnabled
        serviceStatusText.text = if (isEnabled) {
            getString(R.string.service_active)
        } else {
            getString(R.string.service_inactive)
        }
        
        lifecycleScope.launch {
            preferencesRepository.setServiceEnabled(isEnabled)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val componentName = ComponentName(this, EditoAccessibilityService::class.java)
        val enabledServices = TextUtils.SimpleStringSplitter(':')
        enabledServices.setString(accessibilityServices)
        
        while (enabledServices.hasNext()) {
            val enabledService = enabledServices.next()
            val enabledServiceComponentName = ComponentName.unflattenFromString(enabledService)
            if (enabledServiceComponentName != null && enabledServiceComponentName == componentName) {
                return true
            }
        }
        return false
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        checkServiceStatus()
    }
}

