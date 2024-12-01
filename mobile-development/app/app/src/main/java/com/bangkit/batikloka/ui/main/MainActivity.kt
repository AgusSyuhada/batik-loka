package com.bangkit.batikloka.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.catalog.CatalogFragment
import com.bangkit.batikloka.ui.home.HomeFragment
import com.bangkit.batikloka.ui.news.NewsFragment
import com.bangkit.batikloka.ui.user.UserActivity
import com.bangkit.batikloka.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesManager = PreferencesManager(this)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setupBottomNavigation()

        val lastSelectedMenuItem = preferencesManager.getLastSelectedMenuItem()
        bottomNavigationView.selectedItemId = lastSelectedMenuItem
        setFragmentBasedOnMenuItem(lastSelectedMenuItem)
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            preferencesManager.saveLastSelectedMenuItem(menuItem.itemId)

            when (menuItem.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.news -> replaceFragment(NewsFragment())
                R.id.catalog -> replaceFragment(CatalogFragment())
                R.id.user -> {
                    startActivity(Intent(this, UserActivity::class.java))
                    true
                }
                else -> false
            }
        }

        if (supportFragmentManager.fragments.isEmpty()) {
            replaceFragment(HomeFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    private fun setFragmentBasedOnMenuItem(menuItemId: Int) {
        when (menuItemId) {
            R.id.home -> replaceFragment(HomeFragment())
            R.id.news -> replaceFragment(NewsFragment())
            R.id.catalog -> replaceFragment(CatalogFragment())
            else -> replaceFragment(HomeFragment())
        }
    }
}