package com.bangkit.batikloka.ui.tour

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.adapter.TourAdapter
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import java.io.File

class TourActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView
    private lateinit var tourAdapter: TourAdapter
    private lateinit var dotsIndicator: LinearLayout
    private lateinit var tourViewModel: TourViewModel
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.all {
                when (it.key) {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        checkStoragePermission(it.key)
                    }

                    else -> it.value
                }
            }
        } else {
            permissions.all { it.value }
        }

        if (allGranted) {
            initializeTourActivity()
        } else {
            showPermissionRationaleDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)

        requestPermissions()
    }


    private fun requestPermissions() {
        val permissionsToRequest = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val deniedPermissions = permissionsToRequest.filter { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            permissionLauncher.launch(deniedPermissions.toTypedArray())
        } else {
            initializeTourActivity()
        }
    }

    private fun initializeTourActivity() {
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkStoragePermission(permission: String): Boolean {
        return try {
            when (permission) {
                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                    )?.use {
                        it.moveToFirst()
                        true
                    } ?: false
                }

                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    val tempFile = File(cacheDir, "test_write_permission.txt")
                    try {
                        tempFile.createNewFile()
                        tempFile.writeText("Permission Test")
                        true
                    } catch (e: Exception) {
                        false
                    } finally {
                        tempFile.delete()
                    }
                }

                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Izin Diperlukan")
            .setMessage(
                """
            Aplikasi memerlukan izin berikut untuk berfungsi:
            - Akses Internet
            - Kamera
            - Penyimpanan
            
            Mohon berikan izin penuh untuk pengalaman terbaik.
        """.trimIndent()
            )
            .setPositiveButton("Beri Izin") { _, _ ->
                // Buka pengaturan aplikasi secara langsung
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Keluar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun checkAllPermissions(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (checkAllPermissions()) {
                initializeTourActivity()
            } else {
                Toast.makeText(
                    this,
                    "Beberapa izin diperlukan untuk menggunakan aplikasi",
                    Toast.LENGTH_LONG
                ).show()
                finish() // Tutup aktivitas jika izin ditolak
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