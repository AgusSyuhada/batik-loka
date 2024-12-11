package com.bangkit.batikloka.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
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
        val token = preferencesManager.getToken()

        if (!authRepository.isUserLoggedIn()) {
            navigateToLogin()
            return
        }

        setupBottomNavigation()
        navigateToNewsFromNotification()
    }

    private fun navigateToNewsFromNotification() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        if (intent.hasExtra("OPEN_FRAGMENT")) {
            when (intent.getStringExtra("OPEN_FRAGMENT")) {
                "NEWS_FRAGMENT" -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NewsFragment())
                        .commit()

                    bottomNavigationView.selectedItemId = R.id.news
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            val scaleUpAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up_animation)
            fab.startAnimation(scaleUpAnimation)

            startActivity(intent)
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> replaceFragmentWithAnimation(
                    HomeFragment(),
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )

                R.id.news -> replaceFragmentWithAnimation(
                    NewsFragment(),
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )

                R.id.catalog -> replaceFragmentWithAnimation(
                    CatalogFragment(),
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                R.id.user -> {
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.user_slide_up, R.anim.user_slide_down)
                    false
                }
                else -> false
            }
        }

        bottomNavigationView.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.user -> {
                    startActivity(Intent(this, UserActivity::class.java))
                }
            }
        }

        if (supportFragmentManager.fragments.isEmpty()) {
            replaceFragmentWithAnimation(HomeFragment())
        }
    }

    private fun replaceFragmentWithAnimation(
        fragment: Fragment,
        enterAnim: Int = R.anim.slide_in_right,
        exitAnim: Int = R.anim.slide_out_left
    ): Boolean {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.setCustomAnimations(
            enterAnim,
            exitAnim
        )

        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()

        return true
    }

    fun navigateToCatalogFragment() {
        replaceFragmentWithAnimation(CatalogFragment())
        bottomNavigationView.selectedItemId = R.id.catalog
    }
}