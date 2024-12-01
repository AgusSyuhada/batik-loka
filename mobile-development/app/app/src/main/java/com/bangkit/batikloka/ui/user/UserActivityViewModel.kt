package com.bangkit.batikloka.ui.user

import android.util.Log
import androidx.lifecycle.ViewModel

class UserActivityViewModel : ViewModel() {

    fun logImageSourceSelection(source: String) {
        Log.d("UserActivityViewModel", "$source selected")
    }
}