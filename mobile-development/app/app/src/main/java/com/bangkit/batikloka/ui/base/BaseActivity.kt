package com.bangkit.batikloka.ui.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bangkit.batikloka.utils.AppTheme
import com.bangkit.batikloka.utils.PreferencesManager
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager

    override fun attachBaseContext(newBase: Context) {
        preferencesManager = PreferencesManager(newBase)
        val context = updateBaseContextLocale(newBase)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val language = preferencesManager.getCurrentLanguage()
        val locale = when (language) {
            "in" -> Locale("in")
            "en" -> Locale("en")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    private fun applyTheme() {
        val currentTheme = preferencesManager.getCurrentTheme()
        when (currentTheme) {
            AppTheme.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            AppTheme.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            AppTheme.SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    protected fun recreateWithTransition() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(intent)
    }
}