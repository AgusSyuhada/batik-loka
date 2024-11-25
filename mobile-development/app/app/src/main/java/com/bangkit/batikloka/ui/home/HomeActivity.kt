package com.bangkit.batikloka.ui.home
//byrajahafiz//
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}
