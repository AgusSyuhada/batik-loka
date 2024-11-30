package com.bangkit.batikloka.ui.auth.startprofile

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.adapter.ImageSourceAdapter
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.yalantis.ucrop.UCrop
import java.io.File

class StartProfileActivity : AppCompatActivity() {
    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnNext: Button
    private lateinit var viewModel: StartProfileViewModel

    companion object {
        private val PICK_IMAGE_REQUEST = 1
        private val CAMERA_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_profile)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory()
        )[StartProfileViewModel::class.java]

        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        btnNext = findViewById(R.id.btnNext)

        ivProfilePicture.setOnClickListener {
            viewModel.logImageSourceSelection("Profile Picture")
            showImageSourceOptions()
        }

        btnNext.setOnClickListener {
            val preferencesManager = PreferencesManager(this)
            preferencesManager.setUserRegistered()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showImageSourceOptions() {
        val options = arrayOf("Camera", "Gallery")
        val icons = intArrayOf(R.drawable.ic_camera_filled, R.drawable.ic_photo)

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
            titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.caramel_gold))
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
                    imageUri?.let { startCrop(it) }
                }

                CAMERA_REQUEST -> {
                    val bitmap: Bitmap = data?.extras?.get("data") as Bitmap
                    val uri = getImageUri(bitmap)
                    startCrop(uri)
                }

                UCrop.REQUEST_CROP -> {
                    val resultUri = UCrop.getOutput(data!!)
                    if (resultUri != null) {
                        ivProfilePicture.setImageURI(resultUri)
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