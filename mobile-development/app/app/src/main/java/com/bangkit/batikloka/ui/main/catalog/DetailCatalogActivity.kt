package com.bangkit.batikloka.ui.main.catalog

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.model.Batik
import com.bumptech.glide.Glide

class DetailCatalogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_catalog)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val tvProductName: TextView = findViewById(R.id.tv_product_name)
        val ivProductImage: ImageView = findViewById(R.id.iv_product_image)
        val tvProductDescription: TextView = findViewById(R.id.tv_product_description)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val batik = intent.getParcelableExtra<Batik>("BATIK_DATA")

        batik?.let {
            val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
            toolbarTitle.text = it.name

            tvProductName.text = it.name

            Glide.with(this)
                .load(it.image)
                .into(ivProductImage)

            tvProductDescription.text = it.description
        }
    }
}