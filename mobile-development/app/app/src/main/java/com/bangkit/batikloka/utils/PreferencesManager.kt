package com.bangkit.batikloka.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_REGISTERED = "is_registered"
    }

    fun saveUserEmail(email: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_EMAIL, email)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putBoolean(KEY_IS_REGISTERED, false)
        editor.apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isUserRegistered(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_REGISTERED, false)
    }

    fun setUserRegistered() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_REGISTERED, true)
        editor.apply()
    }

    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_USER_EMAIL)
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.putBoolean(KEY_IS_REGISTERED, false)
        editor.apply()
    }
}