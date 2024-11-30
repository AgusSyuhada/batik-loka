package com.bangkit.batikloka.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.catalog.CatalogFragment
import com.bangkit.batikloka.ui.home.HomeFragment
import com.bangkit.batikloka.ui.news.NewsFragment
import com.bangkit.batikloka.ui.user.UserActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val action = intent.getStringExtra("action")
        when (action) {
            "login" -> showCustomAlertDialog("Welcome back to BatikLoka!")
            "register" -> showCustomAlertDialog("Welcome to BatikLoka! Thank you for registering")
            else -> showCustomAlertDialog("Welcome to BatikLoka")
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val fragment: Fragment = HomeFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }

                R.id.news -> {
                    val fragment: Fragment = NewsFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }

                R.id.catalog -> {
                    val fragment: Fragment = CatalogFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                    true
                }

                R.id.user -> {
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }

    private fun showCustomAlertDialog(title: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        titleTextView.text = title

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

            dialog.setCanceledOnTouchOutside(true)
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
    }
}