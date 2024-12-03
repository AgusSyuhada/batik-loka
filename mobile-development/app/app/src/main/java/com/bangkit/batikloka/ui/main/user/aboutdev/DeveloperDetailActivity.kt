package com.bangkit.batikloka.ui.main.user.aboutdev

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Developer
import com.bumptech.glide.Glide

@SuppressLint("SetTextI18n")
class DeveloperDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val developer = intent.getParcelableExtra<Developer>("DEVELOPER")

        developer?.let {
            if (it.image.isNotEmpty()) {
                Glide.with(this)
                    .load(it.image)
                    .placeholder(R.drawable.circle_background)
                    .error(R.drawable.circle_background)
                    .into(findViewById(R.id.profile_picture))
            } else {
                findViewById<ImageView>(R.id.profile_picture)
                    .setImageResource(R.drawable.circle_background)
            }
            findViewById<TextView>(R.id.DeveloperName).text = it.name
            findViewById<TextView>(R.id.LearningPath).text = it.learningPath
            findViewById<TextView>(R.id.tvUniversity).text = it.university
            findViewById<TextView>(R.id.developer_description).text = it.description
            findViewById<TextView>(R.id.toolbar_title).text = it.name

            val detailsLayout: LinearLayout = findViewById(R.id.layout_developer_details)

            detailsLayout.findViewById<TextView>(R.id.name).text = ": ${it.name}"
            detailsLayout.findViewById<TextView>(R.id.age).text = ": ${it.age}"
            detailsLayout.findViewById<TextView>(R.id.major).text = ": ${it.major}"
            detailsLayout.findViewById<TextView>(R.id.university).text = ": ${it.university}"
            detailsLayout.findViewById<TextView>(R.id.domicile).text = ": ${it.domicile}"

            setupSocialMediaLinks(it)
        }
    }

    private fun setupSocialMediaLinks(developer: Developer) {
        findViewById<ImageView>(R.id.instagram_icon).setOnClickListener {
            openSocialMediaLink(developer.instagram)
        }

        findViewById<ImageView>(R.id.linkedin_icon).setOnClickListener {
            openSocialMediaLink(developer.linkedin)
        }

        findViewById<ImageView>(R.id.github_icon).setOnClickListener {
            openSocialMediaLink(developer.github)
        }
    }

    private fun openSocialMediaLink(url: String?) {
        if (url.isNullOrBlank() || url == "-") {
            Toast.makeText(this, "Link not available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }
}