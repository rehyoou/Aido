package com.rr.edito.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.rr.edito.R
import com.rr.edito.ui.adapter.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button
    private lateinit var backButton: Button
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.onboardingViewPager)
        nextButton = findViewById(R.id.nextButton)
        backButton = findViewById(R.id.backButton)

        adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        setupViews()
    }

    private fun setupViews() {
        nextButton.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem++
            } else {
                // Finish onboarding
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        backButton.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem--
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                backButton.visibility = if (position == 0) View.GONE else View.VISIBLE
                nextButton.text = if (position == adapter.itemCount - 1) {
                    "Get Started"
                } else {
                    "Next"
                }
            }
        })
    }
}

