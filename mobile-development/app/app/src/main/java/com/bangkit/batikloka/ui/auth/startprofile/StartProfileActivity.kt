package com.bangkit.batikloka.ui.auth.startprofile

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.ui.adapter.ImageSourceAdapter
import com.bangkit.batikloka.ui.auth.register.RegisterActivity
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

class StartProfileActivity : AppCompatActivity() {
    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnNext: Button
    private lateinit var viewModel: StartProfileViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var database: AppDatabase

    companion object {
        private val PICK_IMAGE_REQUEST = 1
        private val CAMERA_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_profile)

        preferencesManager = PreferencesManager(this)
        database = AppDatabase.getDatabase(this)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager, database)
        )[StartProfileViewModel::class.java]

        checkRegistrationValidity()

        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        btnNext = findViewById(R.id.btnNext)

        ivProfilePicture.setOnClickListener {
            viewModel.logImageSourceSelection("Profile Picture")
            showImageSourceOptions()
        }

        btnNext.setOnClickListener {
            completeRegistration()
        }

        lifecycleScope.launch {
            val savedProfilePicture = viewModel.retrieveProfilePicture()
            savedProfilePicture?.let { bitmap ->
                Glide.with(this@StartProfileActivity)
                    .load(bitmap)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivProfilePicture)
            }
        }
    }

    private fun checkRegistrationValidity() {
        val registrationStep = preferencesManager.getRegistrationStep()
        if (registrationStep == null || registrationStep != "otp_verified") {
            Toast.makeText(this, "Invalid registration process", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
            return
        }
    }

    private fun completeRegistration() {
        preferencesManager.saveRegistrationStep("profile_completed")
        preferencesManager.setUserRegistered()
        preferencesManager.setUserLoggedIn(true)

        showCustomAlertDialog("Welcome to BatikLoka! Thank you for registering") {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showCustomAlertDialog(title: String, onDismiss: () -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        titleTextView.text = title

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
            dialog.setCanceledOnTouchOutside(true)
        }

        dialog.setOnDismissListener {
            onDismiss()
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000)
    }

    private fun showImageSourceOptions() {
        val options = arrayOf("Take photo from camera", "Choose from gallery")
        val icons = intArrayOf(R.drawable.ic_camera_outlined, R.drawable.ic_photo)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image Source")

        val dialogView = layoutInflater.inflate(R.layout.dialog_image_source, null)
        val listView: ListView = dialogView.findViewById(R.id.listView)

        val adapter = ImageSourceAdapter(this, options, icons)
        listView.adapter = adapter

        builder.setView(dialogView)

        val dialog = builder.create()

        dialog.setOnShowListener { dialogInterface ->
            val alertDialog = dialogInterface as AlertDialog
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

            val titleTextView =
                alertDialog.window?.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> openCamera()
                1 -> openGallery()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        Log.d("ImagePicker", "Selected image URI: $imageUri")
                        startCrop(it)
                    }
                }

                CAMERA_REQUEST -> {
                    val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
                    val uri = getImageUri(bitmap)
                    startCrop(uri)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = UCrop.getOutput(data!!)
                    if (resultUri != null) {
                        Glide.with(this)
                            .load(resultUri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(ivProfilePicture)

                        viewModel.saveProfilePicture(resultUri)
                    } else {
                        Toast.makeText(this, "Crop failed", Toast.LENGTH_SHORT).show()
                    }
                }

                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(data!!)
                    Toast.makeText(this, cropError?.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveProfilePictureUri(uri: Uri) {
        preferencesManager.saveProfilePictureUri(uri.toString())
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(500, 500)
            .start(this)
    }

    private fun getImageUri(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }
}
