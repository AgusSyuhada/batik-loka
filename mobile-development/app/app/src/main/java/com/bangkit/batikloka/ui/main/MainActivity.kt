package com.bangkit.batikloka.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.main.catalog.CatalogFragment
import com.bangkit.batikloka.ui.main.home.HomeFragment
import com.bangkit.batikloka.ui.main.news.NewsFragment
import com.bangkit.batikloka.ui.main.scan.ScanActivity
import com.bangkit.batikloka.ui.main.user.UserActivity
import com.bangkit.batikloka.utils.PreferencesManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesManager = PreferencesManager(this)

        preferencesManager = PreferencesManager(this)
        val authApiService = ApiConfig.getAuthApiService(this, preferencesManager)
        authRepository = AuthRepository(authApiService, preferencesManager)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        val token = preferencesManager.getToken()

        if (!authRepository.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setupBottomNavigation()

        val lastSelectedMenuItem = preferencesManager.getLastSelectedMenuItem()
        bottomNavigationView.selectedItemId = lastSelectedMenuItem
        setFragmentBasedOnMenuItem(lastSelectedMenuItem)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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

    fun navigateToCatalogFragment() {
        replaceFragment(CatalogFragment())
        bottomNavigationView.selectedItemId = R.id.catalog
        preferencesManager.saveLastSelectedMenuItem(R.id.catalog)
    }
}