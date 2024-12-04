package com.bangkit.batikloka.ui.main.user.aboutdev

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Developer
import com.bangkit.batikloka.data.model.DevelopersList
import com.bangkit.batikloka.ui.adapter.DeveloperAdapter
import com.google.gson.Gson

class AboutDeveloperActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var developerAdapter: DeveloperAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_developer)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val developers = loadDevelopersFromJson()
        developerAdapter = DeveloperAdapter(developers) { developer ->
            val intent = Intent(this, DeveloperDetailActivity::class.java).apply {
                putExtra("DEVELOPER", developer)
            }
            startActivity(intent)
        }

        recyclerView.adapter = developerAdapter
    }

    private fun loadDevelopersFromJson(): List<Developer> {
        return try {
            val inputStream = assets.open("developers.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val developersList = gson.fromJson(jsonString, DevelopersList::class.java)
            developersList.developers
        } catch (e: Exception) {
            Log.e("LoadDevelopers", "Error loading developers", e)
            emptyList()
        }
    }
}