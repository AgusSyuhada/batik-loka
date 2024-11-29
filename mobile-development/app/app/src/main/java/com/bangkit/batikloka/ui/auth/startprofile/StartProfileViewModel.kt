package com.bangkit.batikloka.ui.auth.startprofile

import android.util.Log
import androidx.lifecycle.ViewModel

class StartProfileViewModel : ViewModel() {

    fun logImageSourceSelection(source: String) {
        Log.d("StartProfileViewModel", "$source selected")
    }
}