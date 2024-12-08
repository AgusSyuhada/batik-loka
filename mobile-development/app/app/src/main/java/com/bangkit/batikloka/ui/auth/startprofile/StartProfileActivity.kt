package com.bangkit.batikloka.ui.auth.startprofile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.databinding.ActivityStartProfileBinding
import com.bangkit.batikloka.ui.adapter.ImageSourceAdapter
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StartProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartProfileBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var preferencesManager: PreferencesManager

    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_PERMISSION_REQUEST = 2
    private val CAMERA_CAPTURE_REQUEST = 3
    private val CROP_IMAGE_REQUEST = 4
    private var currentPhotoPath: String = ""

    companion object {
        fun createAuthRepository(context: Context): AuthRepository {
            val preferencesManager = PreferencesManager(context)
            val authApiService = ApiConfig.getAuthApiService(context, preferencesManager)
            return AuthRepository(authApiService, preferencesManager)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        authRepository = createAuthRepository(this)

        setupProfilePictureListener()
        setupNextButtonListener()
    }

    private fun setupProfilePictureListener() {
        binding.ivProfilePicture.setOnClickListener {
            showImageSourceOptions()
        }
    }

    private fun setupNextButtonListener() {
        binding.btnNext.setOnClickListener {
            // Implementasi logika selanjutnya setelah memilih foto profil
        }
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

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(500, 500)
            .start(this)
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, CAMERA_CAPTURE_REQUEST)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode == Activity.RESULT_OK -> {
                when (requestCode) {
                    PICK_IMAGE_REQUEST -> {
                        data?.data?.let { uri ->
                            startCrop(uri)
                        }
                    }

                    CAMERA_CAPTURE_REQUEST -> {
                        val photoFile = File(currentPhotoPath)
                        val photoURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                this,
                                "${packageName}.fileprovider",
                                photoFile
                            )
                        } else {
                            Uri.fromFile(photoFile)
                        }
                        startCrop(photoURI)
                    }

                    UCrop.REQUEST_CROP -> {
                        val resultUri = UCrop.getOutput(data!!)
                        resultUri?.let { uri ->
                            val file = File(uri.path!!)
                            uploadAvatar(file)
                        }
                    }

                    else -> {
                        Toast.makeText(this, "Unhandled request code", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            resultCode == UCrop.RESULT_ERROR -> {
                val cropError = UCrop.getError(data!!)
                Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadAvatar(file: File) {
        lifecycleScope.launch {
            try {
                showLoading()
                when (val result = authRepository.changeAvatar(file)) {
                    is Result.Success -> {
                        Toast.makeText(
                            this@StartProfileActivity,
                            "Avatar berhasil diupdate",
                            Toast.LENGTH_SHORT
                        ).show()

                        Glide.with(this@StartProfileActivity)
                            .load(file)
                            .into(binding.ivProfilePicture)
                    }

                    is Result.Error -> {
                        Toast.makeText(
                            this@StartProfileActivity,
                            result.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Loading -> {
                        Toast.makeText(
                            this@StartProfileActivity,
                            "Sedang mengunggah...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@StartProfileActivity, "Gagal upload avatar", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                hideLoading()
            }
        }
    }

    private fun showLoading() {
        // Implementasi loading dialog atau progress bar
    }

    private fun hideLoading() {
        // Sembunyikan loading dialog atau progress bar
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
