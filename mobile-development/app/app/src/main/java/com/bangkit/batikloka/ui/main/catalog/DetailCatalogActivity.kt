package com.bangkit.batikloka.ui.main.catalog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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

            val shopeeBtn: LinearLayout = findViewById(R.id.shopee_btn)
            val tokopediaBtn: LinearLayout = findViewById(R.id.tokopedia_btn)

            shopeeBtn.setOnClickListener {
                openSocialMediaLink(batik.shopee)
            }

            tokopediaBtn.setOnClickListener {
                openSocialMediaLink(batik.tokopedia)
            }
        }
    }

    private fun openSocialMediaLink(url: String?) {
        if (url.isNullOrBlank() || url == "-") {
            showCustomErrorDialog(getString(R.string.link_not_available))
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            showCustomErrorDialog(getString(R.string.unable_to_open_link))
        }
    }

    private fun showCustomErrorDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_crossmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_error_title)
        titleTextView.text = message

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
            dialog.setCanceledOnTouchOutside(true)
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000)
    }
}