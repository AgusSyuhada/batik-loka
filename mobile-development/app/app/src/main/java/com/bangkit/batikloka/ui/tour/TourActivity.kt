package com.bangkit.batikloka.ui.tour

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.entity.TourItem
import com.bangkit.batikloka.ui.adapter.TourAdapter
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity

class TourActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView
    private lateinit var tourAdapter: TourAdapter
    private lateinit var dotsIndicator: LinearLayout

    private val tourItems = listOf(
        TourItem(
            imageResId = R.drawable.tour_slide_1,
            titleText = "Scan Batik With Camera",
            descriptionText = "Discover batik patterns and types simply by scanning with your camera"
        ),
        TourItem(
            imageResId = R.drawable.tour_slide_2,
            titleText = "Explore Batik in One Hand",
            descriptionText = "Explore various collections and the stories behind batik right from your hand"
        ),
        TourItem(
            imageResId = R.drawable.tour_slide_3,
            titleText = "Get Batik News Everyday",
            descriptionText = "Stay updated with the latest news and daily inspiration about batik"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)

        // Inisialisasi View
        viewPager = findViewById(R.id.viewPagerWelcomeTour)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        dotsIndicator = findViewById(R.id.dotsIndicator)

        // Setup Adapter
        tourAdapter = TourAdapter(tourItems)
        viewPager.adapter = tourAdapter

        // Setup Dots Indicator
        setupDotsIndicator()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonState(position)
                updateDotsIndicator(position)
            }
        })

        // Setup Button Next
        btnNext.setOnClickListener {
            if (viewPager.currentItem < tourItems.size - 1) {
                viewPager.currentItem++
            } else {
                // Pindah ke RegisterActivity saat di slide terakhir
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }

        // Setup Button Skip
        btnSkip.setOnClickListener {
            if (viewPager.currentItem < tourItems.size - 1) {
                // Saat di slide biasa, tampilkan dialog konfirmasi
                showSkipConfirmationDialog()
            } else {
                // Saat di slide terakhir, pindah ke LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun setupDotsIndicator() {
        for (i in tourItems.indices) {
            val dot = TextView(this)
            dot.text = "•"
            dot.textSize = 36f
            dot.setTextColor(ContextCompat.getColor(this, R.color.light_sand))
            dotsIndicator.addView(dot)
        }
        updateDotsIndicator(0) // Set initial state
    }

    private fun updateDotsIndicator(position: Int) {
        for (i in 0 until dotsIndicator.childCount) {
            val dot = dotsIndicator.getChildAt(i) as TextView
            dot.setTextColor(if (i == position) ContextCompat.getColor(this, R.color.caramel_gold) else ContextCompat.getColor(this, R.color.light_sand))
        }
    }

    private fun showSkipConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Skip Welcome Tour")
            .setMessage("Are you sure you want to skip the welcome tour?")
            .setPositiveButton("Yes") { _, _ ->
                viewPager.currentItem = tourItems.size - 1
            }
            .setNegativeButton("No", null)

        val dialog = builder.create()

        dialog.setOnShowListener { dialogInterface ->
            val alertDialog = dialogInterface as AlertDialog
            // Mengatur latar belakang dialog dengan drawable kustom
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

            // Mengubah warna tombol
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.caramel_gold))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.black))

            // Mengubah warna teks judul
            val titleTextView = alertDialog.window?.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.caramel_gold))
        }

        dialog.show()
    }

    private fun updateButtonState(position: Int) {
        // Update button state berdasarkan posisi slide
        if (position == tourItems.size - 1) {
            // Slide terakhir
            btnNext.text = "Get Started"

            // Mengubah warna teks "Login Here" menjadi caramel_gold dan bold
            val loginText = "Already Have Account? Login Here"
            val spannableString = SpannableString(loginText)

            // Membuat span untuk warna caramel_gold
            val colorSpan = ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.caramel_gold)
            )

            // Membuat span untuk bold
            val boldSpan = android.text.style.StyleSpan(android.graphics.Typeface.BOLD)

            val startIndex = loginText.indexOf("Login Here")
            val endIndex = startIndex + "Login Here".length

            // Menerapkan kedua span pada bagian "Login Here"
            spannableString.setSpan(
                colorSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                boldSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            btnSkip.text = spannableString
        } else {
            // Slide biasa
            btnNext.text = "Next"
            btnSkip.text = "Skip"
        }
    }
}