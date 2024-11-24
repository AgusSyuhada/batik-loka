package com.bangkit.batikloka.ui.auth.register

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.adapter.ImageSourceAdapter
import com.bangkit.batikloka.ui.main.MainActivity
import com.yalantis.ucrop.UCrop
import java.io.File

class StartProfileActivity : AppCompatActivity() {
    private lateinit var ivProfilePicture: ImageView
    private lateinit var btnNext: Button

    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_profile)

        // Inisialisasi View
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        btnNext = findViewById(R.id.btnNext)

        // Setup Click Listeners
        ivProfilePicture.setOnClickListener {
            Log.d("StartProfileActivity", "ivProfilePicture clicked")
            showImageSourceOptions()
        }

        btnNext.setOnClickListener {
            // Pindah ke MainActivity dan kirim informasi bahwa ini adalah pendaftaran
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("action", "register") // Menandakan bahwa ini adalah pendaftaran
            })
        }
    }

    private fun showImageSourceOptions() {
        Log.d("StartProfileActivity", "showImageSourceOptions called")

        val options = arrayOf("Camera", "Gallery")
        val icons = intArrayOf(R.drawable.ic_camera_filled, R.drawable.ic_photo) // Ganti dengan ikon yang sesuai

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image Source")

        // Menggunakan layout kustom untuk dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_image_source, null)
        val listView: ListView = dialogView.findViewById(R.id.listView)

        val adapter = ImageSourceAdapter(this, options, icons)
        listView.adapter = adapter

        builder.setView(dialogView)

        // Buat dialog
        val dialog = builder.create()

        // Mengatur latar belakang dialog dengan drawable kustom
        dialog.setOnShowListener { dialogInterface ->
            val alertDialog = dialogInterface as AlertDialog
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

            // Mengubah warna teks judul
            val titleTextView = alertDialog.window?.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleTextView?.setTextColor(ContextCompat.getColor(this, R.color.caramel_gold))
        }

        // Set listener untuk item di ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> openCamera() // Camera
                1 -> openGallery() // Gallery
            }
            dialog.dismiss() // Tutup dialog setelah memilih
        }

        dialog.show() // Tampilkan dialog
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