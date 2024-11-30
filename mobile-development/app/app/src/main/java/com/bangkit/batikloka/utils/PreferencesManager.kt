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
        private const val KEY_IS_LOGGED_OUT = "is_logged_out"
        private const val KEY_IS_TOUR_COMPLETED = "is_tour_completed"
    }

    fun saveUserEmail(email: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_EMAIL, email)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putBoolean(KEY_IS_REGISTERED, true)
        editor.apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun setUserLoggedOut() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_OUT, true)
        editor.apply()
    }

    fun isUserLoggedOut(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_OUT, false)
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isUserRegistered(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_REGISTERED, false)
    }

    fun setUserRegistered(isRegistered: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_REGISTERED, isRegistered)
        editor.apply()
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