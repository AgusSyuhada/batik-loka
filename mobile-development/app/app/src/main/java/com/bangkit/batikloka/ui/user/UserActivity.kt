package com.bangkit.batikloka.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.utils.PreferencesManager

class UserActivity : AppCompatActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var logoutContainer: View
    private lateinit var icLogout: ImageView
    private lateinit var tvLogout: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        preferencesManager = PreferencesManager(this)

        logoutContainer = findViewById(R.id.logoutContainer)
        icLogout = findViewById(R.id.ic_logout)
        tvLogout = findViewById(R.id.tv_logout)

        setupLogoutListener()
    }

    private fun setupLogoutListener() {
        logoutContainer.setOnClickListener {
            performLogout()
        }

        icLogout.setOnClickListener {
            performLogout()
        }

        tvLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        preferencesManager.setUserLoggedOut()

        preferencesManager.clearUserData()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}