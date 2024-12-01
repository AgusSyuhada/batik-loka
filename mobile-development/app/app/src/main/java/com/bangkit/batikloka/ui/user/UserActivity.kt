package com.bangkit.batikloka.ui.user

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.adapter.ImageSourceAdapter
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.yalantis.ucrop.UCrop
import java.io.File

class UserActivity : AppCompatActivity() {
    private lateinit var ivProfilePicture: ImageView
    private lateinit var viewModel: UserActivityViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var logoutContainer: View
    private lateinit var toolbar: Toolbar

    companion object {
        private val PICK_IMAGE_REQUEST = 1
        private val CAMERA_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        preferencesManager = PreferencesManager(this)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager)
        )[UserActivityViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        logoutContainer = findViewById(R.id.layout_logout)
        ivProfilePicture = findViewById(R.id.iv_profile_picture)

        ivProfilePicture.setOnClickListener {
            viewModel.logImageSourceSelection("Profile Picture")
            showImageSourceOptions()
        }

        setupLogoutListener()

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupLogoutListener() {
        logoutContainer.setOnClickListener {
            performLogout()
        }
    }

    private fun getPreviousMenuItem(): Int {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("LAST_SELECTED_MENU_ITEM", R.id.home)
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

    private fun performLogout() {
        preferencesManager.setUserLoggedOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}