package com.bangkit.batikloka.ui.tour

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bangkit.batikloka.R
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

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)

        preferencesManager = PreferencesManager(this)
        tourViewModel = ViewModelProvider(
            this,
            AppViewModelFactory(preferencesManager)
        )[TourViewModel::class.java]

        viewPager = findViewById(R.id.viewPagerWelcomeTour)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        dotsIndicator = findViewById(R.id.dotsIndicator)

        tourAdapter = TourAdapter(tourViewModel.tourItems)
        viewPager.adapter = tourAdapter

        setupDotsIndicator()
        checkPermissions()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonState(position)
                updateDotsIndicator(position)
            }
        })

        btnNext.setOnClickListener { onNextButtonClicked() }
        btnSkip.setOnClickListener { onSkipButtonClicked() }
    }

    private fun checkPermissions() {
        if (!allPermissionsGranted()) {
            requestAllPermissions()
        }
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    private fun setupDotsIndicator() {
        for (i in 0 until tourViewModel.getTourItemCount()) {
            val dot = TextView(this).apply {
                text = "•"
                textSize = 36f
                setTextColor(ContextCompat.getColor(this@TourActivity, R.color.light_sand))
            }
            dotsIndicator.addView(dot)
        }
        updateDotsIndicator(0)
    }

    private fun updateDotsIndicator(position: Int) {
        for (i in 0 until dotsIndicator.childCount) {
            val dot = dotsIndicator.getChildAt(i) as TextView
            dot.setTextColor(
                if (i == position) ContextCompat.getColor(this, R.color.caramel_gold)
                else ContextCompat.getColor(this, R.color.light_sand)
            )
        }
    }

    private fun onNextButtonClicked() {
        if (viewPager.currentItem < tourViewModel.getTourItemCount() - 1) {
            viewPager.currentItem++
        } else {
            preferencesManager.setTourCompleted()
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun onSkipButtonClicked() {
        if (viewPager.currentItem < tourViewModel.getTourItemCount() - 1) {
            showSkipConfirmationDialog()
        } else {
            preferencesManager.setTourCompleted()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.caramel_gold))
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

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permission_denied_message))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }

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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
            } else {
                val showRationale = permissions.any {
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                }

                if (showRationale) {
                    showPermissionDeniedDialog()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permissions_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}