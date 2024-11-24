package com.bangkit.batikloka.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.tour.TourActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        hideActionBar()
        navigateToMainActivity()
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }

    private fun navigateToMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, TourActivity::class.java))
            finish()
        }, SPLASH_SCREEN_DURATION)
    }

    companion object {
        private const val SPLASH_SCREEN_DURATION = 3000L
    }
}