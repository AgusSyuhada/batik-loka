package com.bangkit.batikloka.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import java.util.Locale

class PreferencesManager(private val context: Context) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_REGISTERED = "is_registered"
        private const val KEY_IS_LOGGED_OUT = "is_logged_out"
        private const val KEY_IS_TOUR_COMPLETED = "is_tour_completed"
        private const val KEY_REGISTRATION_STEP = "registration_step"
        private const val KEY_IS_RESET_PASSWORD = "is_reset_password"
        private const val KEY_VERIFICATION_OTP = "verification_otp"
        private const val KEY_OTP_TIMESTAMP = "otp_timestamp"
        private const val PREF_PROFILE_IMAGE_URL = "pref_profile_image_url"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_APP_LANGUAGE = "app_language"
    }

    fun isNewsNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean("news_notification_enabled", false)
    }

    fun setNewsNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("news_notification_enabled", enabled).apply()
    }

    fun saveTheme(theme: AppTheme) {
        sharedPreferences.edit {
            putString(KEY_APP_THEME, theme.name)
        }
        applyTheme(theme)
    }

    fun getCurrentTheme(): AppTheme {
        val themeName = sharedPreferences.getString(KEY_APP_THEME, AppTheme.SYSTEM.name)
        return AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
    }

    fun applyTheme(theme: AppTheme = getCurrentTheme()) {
        when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppTheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            AppTheme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(KEY_APP_LANGUAGE, "system") ?: "system"
    }

    fun setLanguage(languageCode: String) {
        sharedPreferences.edit {
            putString(KEY_APP_LANGUAGE, languageCode)
        }
        updateAppLanguage(languageCode)
    }

    fun setSystemDefaultLanguage() {
        setLanguage("system")
    }

    fun setIndonesianLanguage() {
        setLanguage("id")
    }

    fun setEnglishLanguage() {
        setLanguage("en")
    }

    private fun updateAppLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "system" -> Locale.getDefault()
            "id" -> Locale("id")
            "en" -> Locale("en")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun applyLanguageFromPreferences() {
        val languageCode = getCurrentLanguage()
        updateAppLanguage(languageCode)
    }

    fun getSelectedLanguage(): String {
        return sharedPreferences.getString("language", "system") ?: "system"
    }

    fun saveToken(token: String?) {
        token?.let { nonNullToken ->
            sharedPreferences.edit().apply {
                putString("bearer_token", nonNullToken)
                putBoolean("is_logged_in", true)
            }.apply()

            Log.d("TokenManager", "Token saved: $nonNullToken")
        } ?: run {
            Log.e("TokenManager", "Attempted to save null token")
        }
    }

    fun clearResetPasswordData() {
        sharedPreferences.edit().apply {
            remove(KEY_IS_RESET_PASSWORD)
            remove("reset_password_email")
            remove(KEY_VERIFICATION_OTP)
            remove(KEY_OTP_TIMESTAMP)
            remove(KEY_REGISTRATION_STEP)
        }.apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("bearer_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("bearer_token")
            .remove("is_logged_in")
            .apply()
    }

    fun isUserLoggedIn(): Boolean {
        return getToken() != null
    }

    fun getAccessToken(): String? {
        val token = sharedPreferences.getString("access_token", null)
        Log.d("PreferencesManager", "Get access token: ${token ?: "No token"}")
        return token
    }

    fun saveProfileImageUrl(imageUrl: String) {
        sharedPreferences.edit().putString(PREF_PROFILE_IMAGE_URL, imageUrl).apply()
    }

    fun getProfileImageUrl(): String? {
        return sharedPreferences.getString(PREF_PROFILE_IMAGE_URL, null)
    }

    fun isUserLoggedOut(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_OUT, false)
    }

    fun isUserRegistered(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_REGISTERED, false)
    }

    fun getRegistrationStep(): String? {
        return sharedPreferences.getString("registration_step", null)
    }

    fun setTourCompleted() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_TOUR_COMPLETED, true)
        editor.apply()
    }

    fun isTourCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_TOUR_COMPLETED, false)
    }
}