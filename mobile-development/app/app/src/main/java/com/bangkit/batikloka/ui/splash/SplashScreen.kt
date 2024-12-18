package com.bangkit.batikloka.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.tour.TourActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private lateinit var splashViewModel: SplashScreenViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        preferencesManager = PreferencesManager(this)
        applyLanguageFromPreferences()
        preferencesManager.applyTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        splashViewModel = ViewModelProvider(
            this,
            AppViewModelFactory(preferencesManager)
        )[SplashScreenViewModel::class.java]

        hideActionBar()
        navigateToNextActivity()
    }

    private fun applyLanguageFromPreferences() {
        val currentLanguage = getCurrentLanguage()
        val savedLanguage = preferencesManager.getSelectedLanguage()

        if (currentLanguage != savedLanguage) {
            when (savedLanguage) {
                "system" -> preferencesManager.setSystemDefaultLanguage()
                "id" -> preferencesManager.setIndonesianLanguage()
                "en" -> preferencesManager.setEnglishLanguage()
                else -> preferencesManager.setSystemDefaultLanguage()
            }
        }
    }

    private fun getCurrentLanguage(): String {
        return when (resources.configuration.locales.get(0).language) {
            "in" -> "id"
            "en" -> "en"
            else -> "system"
        }
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }

    private fun navigateToNextActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            when {
                preferencesManager.isUserLoggedOut() -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                    return@postDelayed
                }

                preferencesManager.isUserLoggedIn() -> {
                    when (preferencesManager.getRegistrationStep()) {
                        "profile_completed" -> {
                            startActivity(Intent(this, MainActivity::class.java))
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }

                        else -> {
                            startActivity(Intent(this, LoginActivity::class.java))
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        }
                    }
                }

                preferencesManager.isTourCompleted() -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }

                else -> {
                    startActivity(Intent(this, TourActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            }

            finish()
        }, SPLASH_SCREEN_DURATION)
    }

    companion object {
        private const val SPLASH_SCREEN_DURATION = 2000L
    }
}