package com.bangkit.batikloka.utils

import android.content.Context
import android.content.SharedPreferences
import com.bangkit.batikloka.R

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_REGISTERED = "is_registered"
        private const val KEY_IS_LOGGED_OUT = "is_logged_out"
        private const val KEY_IS_TOUR_COMPLETED = "is_tour_completed"
        private const val KEY_REGISTRATION_STEP = "registration_step"
        private const val KEY_IS_RESET_PASSWORD = "is_reset_password"
        private const val KEY_LAST_SELECTED_MENU_ITEM = "last_selected_menu_item"
        private const val KEY_PROFILE_PICTURE_URI = "profile_picture_uri"
        private const val KEY_USER_NAME = "user_name"
    }

    fun saveProfilePictureUri(uriString: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_PROFILE_PICTURE_URI, uriString)
        editor.apply()
    }

    fun saveUserName(name: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_NAME, name)
        editor.apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun savePassword(password: String) {
        val editor = sharedPreferences.edit()
        editor.putString("user_password", password)
        editor.apply()
    }

    fun getStoredPassword(): String? {
        return sharedPreferences.getString("user_password", null)
    }

    fun getProfilePictureUri(): String? {
        return sharedPreferences.getString(KEY_PROFILE_PICTURE_URI, null)
    }

    fun saveLastSelectedMenuItem(menuItemId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_LAST_SELECTED_MENU_ITEM, menuItemId)
        editor.apply()
    }

    fun getLastSelectedMenuItem(): Int {
        return sharedPreferences.getInt(KEY_LAST_SELECTED_MENU_ITEM, R.id.home)
    }

    fun clearLastSelectedMenuItem() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_LAST_SELECTED_MENU_ITEM)
        editor.apply()
    }

    fun resetTourStatus() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_IS_TOUR_COMPLETED)
        editor.apply()
    }

    fun setResetPasswordStatus(isReset: Boolean, email: String? = null) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_RESET_PASSWORD, isReset)
        email?.let {
            editor.putString("reset_password_email", it)
        }
        editor.apply()
    }

    fun getResetPasswordEmail(): String? {
        return sharedPreferences.getString("reset_password_email", null)
    }

    fun clearResetPasswordData() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_IS_RESET_PASSWORD)
        editor.remove("reset_password_email")
        editor.apply()
    }

    fun isResetPassword(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_RESET_PASSWORD, false)
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

    fun clearLogoutStatus() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_IS_LOGGED_OUT)
        editor.apply()
    }

    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun setUserLoggedOut() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_OUT, true)
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
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

    fun setUserRegistered() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_REGISTERED, true)
        editor.apply()
    }

    fun saveRegistrationStep(step: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_REGISTRATION_STEP, step)
        editor.apply()
    }

    fun getRegistrationStep(): String? {
        return sharedPreferences.getString(KEY_REGISTRATION_STEP, null)
    }

    fun resetRegistrationProcess() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_REGISTRATION_STEP)
        editor.remove(KEY_USER_EMAIL)
        editor.remove(KEY_IS_REGISTERED)
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