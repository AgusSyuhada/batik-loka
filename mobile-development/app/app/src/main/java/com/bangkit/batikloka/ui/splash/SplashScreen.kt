package com.bangkit.batikloka.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.codeverification.VerificationActivity
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.startprofile.StartProfileActivity
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.tour.TourActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private lateinit var splashViewModel: SplashScreenViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        preferencesManager = PreferencesManager(this)
        splashViewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager)
        )[SplashScreenViewModel::class.java]

        hideActionBar()
        navigateToNextActivity()
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }

    private fun navigateToNextActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            when {
                preferencesManager.isUserLoggedOut() -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    return@postDelayed
                }

                splashViewModel.isUserLoggedIn() -> {
                    when (preferencesManager.getRegistrationStep()) {
                        "email_registered" -> startActivity(
                            Intent(
                                this,
                                VerificationActivity::class.java
                            )
                        )

                        "otp_verified" -> startActivity(
                            Intent(
                                this,
                                StartProfileActivity::class.java
                            )
                        )

                        else -> startActivity(Intent(this, MainActivity::class.java))
                    }
                }

                preferencesManager.isTourCompleted() -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }

                else -> {
                    startActivity(Intent(this, TourActivity::class.java))
                }
            }
            finish()
        }, SPLASH_SCREEN_DURATION)
    }

    companion object {
        private const val SPLASH_SCREEN_DURATION = 2000L
    }
}