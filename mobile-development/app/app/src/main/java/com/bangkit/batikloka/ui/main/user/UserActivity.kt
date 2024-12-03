package com.bangkit.batikloka.ui.main.user

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.ui.adapter.ImageSourceAdapter
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.main.user.aboutdev.AboutDeveloperActivity
import com.bangkit.batikloka.ui.main.user.privpol.PrivacyPolicyActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File

class UserActivity : AppCompatActivity() {
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvShowEmail: TextView
    private lateinit var tvUsername: TextView
    private lateinit var viewModel: UserActivityViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var logoutContainer: View
    private lateinit var toolbar: Toolbar
    private lateinit var layoutEditName: ConstraintLayout
    private lateinit var layoutChangePassword: ConstraintLayout
    private lateinit var database: AppDatabase

    companion object {
        private val PICK_IMAGE_REQUEST = 1
        private val CAMERA_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        preferencesManager = PreferencesManager(this)
        database = AppDatabase.getDatabase(this)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager, database)
        )[UserActivityViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        logoutContainer = findViewById(R.id.layout_logout)
        ivProfilePicture = findViewById(R.id.iv_profile_picture)
        tvShowEmail = findViewById(R.id.tv_show_email)
        tvUsername = findViewById(R.id.text_username)

        ivProfilePicture.setOnClickListener {
            viewModel.logImageSourceSelection("Profile Picture")
            showImageSourceOptions()
        }

        val ivEditProfilePicture: ImageView = findViewById(R.id.ivEditProfilePicture)
        ivEditProfilePicture.setOnClickListener {
            viewModel.logImageSourceSelection("Profile Picture Edit Icon")
            showImageSourceOptions()
        }

        layoutEditName = findViewById(R.id.layout_edit_name)
        layoutEditName.setOnClickListener {
            showEditNameDialog()
        }

        layoutChangePassword = findViewById(R.id.layout_change_password)
        layoutChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        val layoutAboutDeveloper: ConstraintLayout = findViewById(R.id.layout_about_developer)
        layoutAboutDeveloper.setOnClickListener {
            val intent = Intent(this, AboutDeveloperActivity::class.java)
            startActivity(intent)
        }

        val layoutPrivacyPolicyActivity: ConstraintLayout = findViewById(R.id.layout_privacy_policy)
        layoutPrivacyPolicyActivity.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        setupLogoutListener()

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        displayUserEmail()
        displayUsername()
        loadSavedProfilePicture()
    }

    private fun displayUserEmail() {
        val userEmail = preferencesManager.getUserEmail()
        if (userEmail != null) {
            tvShowEmail.text = userEmail
        } else {
            tvShowEmail.text = "Email not found"
        }
    }

    private fun displayUsername() {
        val username = preferencesManager.getUserName()
        if (username != null) {
            tvUsername.text = username
        } else {
            tvUsername.text = "Username not found"
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

    private fun loadSavedProfilePicture() {
        lifecycleScope.launch {
            val profileBitmap = viewModel.retrieveProfilePicture()

            profileBitmap?.let { bitmap ->
                Glide.with(this@UserActivity)
                    .load(bitmap)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivProfilePicture)
            }
        }
    }

    private fun showEditNameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
        val btnSave = dialogView.findViewById<TextView>(R.id.btnSaveName)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancel)

        val currentName = preferencesManager.getUserName() ?: ""
        etName.setText(currentName)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
        }

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()

            if (newName.isEmpty()) {
                etName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (newName.length < 3) {
                etName.error = "Name must be at least 3 characters"
                return@setOnClickListener
            }

            viewModel.updateUsername(newName)
            preferencesManager.saveUserName(newName)
            tvUsername.text = newName

            showCustomAlertDialog("Name successfully updated") {
                // Aksi tambahan jika diperlukan
            }

            dialog.dismiss()
        }

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)

        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.et_current_password)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.et_new_password)
        val etConfirmNewPassword = dialogView.findViewById<EditText>(R.id.et_confirm_new_password)

        val ivShowCurrentPassword =
            dialogView.findViewById<ImageView>(R.id.iv_show_current_password)
        val ivShowNewPassword = dialogView.findViewById<ImageView>(R.id.iv_show_new_password)
        val ivShowConfirmPassword =
            dialogView.findViewById<ImageView>(R.id.iv_show_confirm_password)

        val btnSavePassword = dialogView.findViewById<TextView>(R.id.btn_save_password)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel_change_password)

        var isCurrentPasswordVisible = false
        var isNewPasswordVisible = false
        var isConfirmPasswordVisible = false

        ivShowCurrentPassword.setOnClickListener {
            isCurrentPasswordVisible = !isCurrentPasswordVisible
            togglePasswordVisibility(
                etCurrentPassword,
                ivShowCurrentPassword,
                isCurrentPasswordVisible
            )
        }

        ivShowNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(
                etNewPassword,
                ivShowNewPassword,
                isNewPasswordVisible
            )
        }

        ivShowConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(
                etConfirmNewPassword,
                ivShowConfirmPassword,
                isConfirmPasswordVisible
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
        }

        btnSavePassword.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmNewPassword = etConfirmNewPassword.text.toString().trim()

            val userEmail = preferencesManager.getUserEmail() ?: return@setOnClickListener

            lifecycleScope.launch {
                when {
                    currentPassword.isEmpty() -> {
                        etCurrentPassword.error = "Current password cannot be empty"
                        return@launch
                    }

                    newPassword.isEmpty() -> {
                        etNewPassword.error = "New password cannot be empty"
                        return@launch
                    }

                    newPassword.length < 6 -> {
                        etNewPassword.error = "Password must be at least 6 characters"
                        return@launch
                    }

                    newPassword == currentPassword -> {
                        etNewPassword.error = "New password must be different from current password"
                        return@launch
                    }

                    newPassword != confirmNewPassword -> {
                        etConfirmNewPassword.error = "Passwords do not match"
                        return@launch
                    }

                    else -> {
                        val isPasswordUpdated =
                            viewModel.updatePassword(userEmail, currentPassword, newPassword)

                        if (isPasswordUpdated) {
                            preferencesManager.savePassword(newPassword)

                            showCustomAlertDialog("Password successfully changed") {
                                // Aksi tambahan jika diperlukan
                            }
                            dialog.dismiss()
                        } else {
                            etCurrentPassword.error = "Current password is incorrect"
                        }
                    }
                }
            }
        }

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        imageView: ImageView,
        isVisible: Boolean,
    ) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.ic_visibility)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.ic_visibility_off)
        }
        editText.setSelection(editText.text.length)
    }

    private fun isCurrentPasswordCorrect(currentPassword: String): Boolean {
        val storedPassword = preferencesManager.getStoredPassword()
        return currentPassword == storedPassword
    }

    private fun updatePassword(newPassword: String) {
        preferencesManager.savePassword(newPassword)

        showCustomAlertDialog("Password successfully changed") {
            // Aksi tambahan jika diperlukan
        }
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
                        viewModel.updateProfilePicture(resultUri)

                        preferencesManager.saveProfilePictureUri(resultUri.toString())

                        Glide.with(this)
                            .load(resultUri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(ivProfilePicture)
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
        showCustomAlertDialog("Logout Successful! See you next time") {
            preferencesManager.setUserLoggedOut()
            preferencesManager.resetRegistrationProcess()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}