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
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.ui.adapter.TourAdapter
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager

class TourActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView
    private lateinit var tourAdapter: TourAdapter
    private lateinit var dotsIndicator: LinearLayout
    private lateinit var tourViewModel: TourViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)

        preferencesManager = PreferencesManager(this)
        database = AppDatabase.getDatabase(this)

        tourViewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager, database)
        )[TourViewModel::class.java]


        viewPager = findViewById(R.id.viewPagerWelcomeTour)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        dotsIndicator = findViewById(R.id.dotsIndicator)

        tourAdapter = TourAdapter(tourViewModel.tourItems)
        viewPager.adapter = tourAdapter

        setupDotsIndicator()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonState(position)
                updateDotsIndicator(position)
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < tourViewModel.getTourItemCount() - 1) {
                viewPager.currentItem++
            } else {
                preferencesManager.setTourCompleted()
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }

        btnSkip.setOnClickListener {
            if (viewPager.currentItem < tourViewModel.getTourItemCount() - 1) {
                showSkipConfirmationDialog()
            } else {
                preferencesManager.setTourCompleted()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun setupDotsIndicator() {
        for (i in 0 until tourViewModel.getTourItemCount()) {
            val dot = TextView(this)
            dot.text = "•"
            dot.textSize = 36f
            dot.setTextColor(ContextCompat.getColor(this, R.color.light_sand))
            dotsIndicator.addView(dot)
        }
        updateDotsIndicator(0)
    }

    private fun updateDotsIndicator(position: Int) {
        for (i in 0 until dotsIndicator.childCount) {
            val dot = dotsIndicator.getChildAt(i) as TextView
            dot.setTextColor(
                if (i == position) ContextCompat.getColor(
                    this,
                    R.color.caramel_gold
                ) else ContextCompat.getColor(this, R.color.light_sand)
            )
        }
    }

    private fun showSkipConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.skip_tour_dialog_title)
            .setMessage(R.string.skip_tour_dialog_message)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                viewPager.currentItem = tourViewModel.getTourItemCount() - 1
            }
            .setNegativeButton(R.string.dialog_no, null)

        val dialog = builder.create()

        dialog.setOnShowListener { dialogInterface ->
            val alertDialog = dialogInterface as AlertDialog
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.caramel_gold))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.black))

            val titleTextView =
                alertDialog.window?.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.caramel_gold))
        }

        dialog.show()
    }

    private fun updateButtonState(position: Int) {
        if (position == tourViewModel.getTourItemCount() - 1) {
            btnNext.text = getString(R.string.btn_get_started)

            val fullText = getString(R.string.login_hint)
            val loginHereText =
                if (fullText.contains("Login Here")) "Login Here" else "Login Di Sini"

            val spannableString = SpannableString(fullText)

            val colorSpan = ForegroundColorSpan(
                ContextCompat.getColor(this, R.color.caramel_gold)
            )

            val boldSpan = android.text.style.StyleSpan(android.graphics.Typeface.BOLD)

            val startIndex = fullText.indexOf(loginHereText)
            if (startIndex != -1) {
                val endIndex = startIndex + loginHereText.length

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
            }

            btnSkip.text = spannableString
        } else {
            btnNext.text = getString(R.string.btn_next)
            btnSkip.text = getString(R.string.btn_skip)
        }
    }
}