package com.bangkit.batikloka.ui.main.user

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.remote.response.ChangeAvatarResponse
import com.bangkit.batikloka.data.remote.response.ForgetPasswordResponse
import com.bangkit.batikloka.data.remote.response.ProfileResponse
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.databinding.ActivityUserBinding
import com.bangkit.batikloka.ui.adapter.LanguageAdapter
import com.bangkit.batikloka.ui.adapter.ThemeAdapter
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.base.BaseActivity
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.ui.main.user.aboutdev.AboutDeveloperActivity
import com.bangkit.batikloka.ui.main.user.historyscan.HistoryScanActivity
import com.bangkit.batikloka.ui.main.user.privpol.PrivacyPolicyActivity
import com.bangkit.batikloka.ui.main.user.viewmodel.UserActivityViewModel
import com.bangkit.batikloka.ui.main.user.viewmodel.UserViewModelFactory
import com.bangkit.batikloka.utils.AppTheme
import com.bangkit.batikloka.utils.ImagePickerHelper
import com.bangkit.batikloka.utils.NewsNotificationHelper
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result
import com.bumptech.glide.Glide
import java.io.File

@SuppressLint("UseSwitchCompatOrMaterialCode")
class UserActivity : BaseActivity() {

    private lateinit var binding: ActivityUserBinding
    private lateinit var viewModel: UserActivityViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private val imagePickerHelper = ImagePickerHelper(this)
    private lateinit var switchNewsNotification: Switch
    private lateinit var newsNotificationHelper: NewsNotificationHelper
    private lateinit var workManager: WorkManager

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002

        fun createAuthRepository(context: Context): AuthRepository {
            val preferencesManager = PreferencesManager(context)
            val authApiService = ApiConfig.getAuthApiService(context, preferencesManager)
            return AuthRepository(authApiService, preferencesManager)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this
        authRepository = createAuthRepository(this)
        preferencesManager = PreferencesManager(this)
        preferencesManager.applyLanguageFromPreferences()

        val viewModelFactory = UserViewModelFactory(authRepository, context)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserActivityViewModel::class.java]

        newsNotificationHelper = NewsNotificationHelper(this)
        workManager = WorkManager.getInstance(this)
        switchNewsNotification = binding.switchNewsNotification

        setupNewsNotificationSwitch()
        setupToolbar()
        setupObservers()
        setupPrivacyPolicyListener()
        setupAboutDeveloperListener()
        setupHistoryScanListener()
        setupListeners()

        viewModel.fetchProfile()

        imagePickerHelper.setOnImageSelectedListener { file ->
            uploadAvatar(file)
        }
    }

    private fun setupToolbar() {
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.title = ""

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.profileResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val profile = result.data as ProfileResponse
                    updateProfileUI(profile)
                }

                is Result.Error -> {
                    showCustomErrorDialog(result.message)
                }

                is Result.Loading -> {
                    // Optional: Show loading indicator
                }

                null -> {
                    //
                }
            }
        }

        viewModel.logoutResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    showCustomAlertDialog(getString(R.string.logout_success)) {
                        navigateToLogin()
                    }
                }

                is Result.Error -> {
                    showCustomErrorDialog(result.message)
                }

                is Result.Loading -> {
                    // Optional: Show loading indicator
                }

                null -> {
                    // Optional: Handle null case
                }
            }
        }

        viewModel.changeNameResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    showCustomAlertDialog(getString(R.string.name_change_success)) {
                        viewModel.fetchProfile()
                    }
                }

                is Result.Error -> {
                    showCustomErrorDialog(result.message)
                }

                is Result.Loading -> {
                    // Optional: Show loading indicator
                }

                null -> {
                    // Optional: Handle null case
                }
            }
        }

        viewModel.avatarResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    val avatarUrl = (result.data as ChangeAvatarResponse).avatarUrl
                    preferencesManager.saveProfileImageUrl(avatarUrl)

                    Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar_outlined_250)
                        .error(R.drawable.ic_avatar_outlined_250)
                        .into(binding.ivProfilePicture)

                    showCustomAlertDialog(getString(R.string.avatar_update_success)) {
                        // Optional: Handle success
                    }
                }

                is Result.Error -> {
                    showCustomErrorDialog(result.message)
                }

                is Result.Loading -> {
                    // Optional: Show loading indicator
                }

                null -> {
                    // Optional: Handle null case
                }
            }
        }

        viewModel.changePasswordResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    dismissLoadingDialog()
                    if (result.data is ForgetPasswordResponse) {
                        val userEmail = binding.tvShowEmail.text.toString().trim()
                        showResetPasswordDialog(userEmail)
                    } else {
                        showCustomAlertDialog(getString(R.string.password_reset_success)) {
                            navigateToLogin()
                        }
                    }
                }

                is Result.Error -> {
                    dismissLoadingDialog()
                    handleForgetPasswordError(result.message)
                }

                is Result.Loading -> {
                    showLoadingDialog(getString(R.string.loading_message))
                }

                null -> {
                    dismissLoadingDialog()
                }
            }
        }
    }

    private fun setupPasswordVisibilityListeners(
        etNewPassword: EditText,
        etConfirmNewPassword: EditText,
        ivShowNewPassword: ImageView,
        ivShowConfirmNewPassword: ImageView
    ) {
        var isNewPasswordVisible = false
        var isConfirmPasswordVisible = false

        ivShowNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(etNewPassword, ivShowNewPassword, isNewPasswordVisible)
        }

        ivShowConfirmNewPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(
                etConfirmNewPassword,
                ivShowConfirmNewPassword,
                isConfirmPasswordVisible
            )
        }
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        imageView: ImageView,
        isVisible: Boolean
    ) {
        val cursorPosition = editText.selectionStart

        editText.transformationMethod = if (isVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }

        imageView.setImageResource(
            if (isVisible) R.drawable.ic_visibility
            else R.drawable.ic_visibility_off
        )

        editText.setSelection(cursorPosition)

        editText.requestFocus()
    }

    private fun showChangePasswordDialog() {
        val userEmail = binding.tvShowEmail.text.toString().trim()

        if (userEmail.isEmpty()) {
            showCustomErrorDialog(getString(R.string.email_not_found))
            return
        }

        viewModel.forgetPassword(userEmail)
    }

    private var loadingDialog: ProgressDialog? = null

    private fun showLoadingDialog(message: String) {
        loadingDialog?.dismiss()
        loadingDialog = ProgressDialog(this)
        loadingDialog?.setMessage(message)
        loadingDialog?.setCancelable(true)
        loadingDialog?.setOnCancelListener {
            viewModel.cancelCurrentOperation()
        }
        loadingDialog?.setOnShowListener {
            loadingDialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
        }
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun showResetPasswordDialog(email: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)

        val etVerificationCode = dialogView.findViewById<EditText>(R.id.et_verification_code)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.et_new_password)
        val etConfirmNewPassword = dialogView.findViewById<EditText>(R.id.et_confirm_new_password)

        val ivShowNewPassword = dialogView.findViewById<ImageView>(R.id.iv_show_new_password)
        val ivShowConfirmPassword =
            dialogView.findViewById<ImageView>(R.id.iv_show_confirm_password)

        val btnSavePassword = dialogView.findViewById<TextView>(R.id.btn_save_password)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tv_cancel_change_password)

        setupPasswordVisibilityListeners(
            etNewPassword,
            etConfirmNewPassword,
            ivShowNewPassword,
            ivShowConfirmPassword
        )

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
        }

        btnSavePassword.setOnClickListener {
            val verificationCode = etVerificationCode.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmNewPassword = etConfirmNewPassword.text.toString().trim()

            when {
                verificationCode.isEmpty() || verificationCode.length != 6 -> {
                    etVerificationCode.error = getString(R.string.verification_code_error)
                    return@setOnClickListener
                }

                newPassword.isEmpty() -> {
                    etNewPassword.error = getString(R.string.new_password_empty)
                    return@setOnClickListener
                }

                newPassword.length < 6 -> {
                    etNewPassword.error = getString(R.string.new_password_length_error)
                    return@setOnClickListener
                }

                newPassword != confirmNewPassword -> {
                    etConfirmNewPassword.error = getString(R.string.password_confirmation_error)
                    return@setOnClickListener
                }

                else -> {
                    viewModel.resetPassword(
                        email,
                        verificationCode,
                        newPassword
                    )
                    dialog.dismiss()
                }
            }
        }

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleForgetPasswordError(errorMessage: String) {
        when {
            errorMessage.contains("User  not found", ignoreCase = true) -> {
                showCustomErrorDialog(getString(R.string.user_not_found))
            }

            errorMessage.contains("required", ignoreCase = true) -> {
                showCustomErrorDialog(getString(R.string.email_required))
            }

            else -> {
                showCustomErrorDialog(errorMessage)
            }
        }
    }

    private fun showCustomAlertDialog(title: String, onDismiss: () -> Unit = {}) {
        if (isFinishing || isDestroyed) {
            onDismiss()
            return
        }

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

        if (!isFinishing && !isDestroyed) {
            dialog.show()

            Handler(Looper.getMainLooper()).postDelayed({
                if (dialog.isShowing && !isFinishing && !isDestroyed) {
                    dialog.dismiss()
                }
            }, 2000)
        } else {
            onDismiss()
        }
    }

    private fun setupListeners() {
        binding.layoutLogout.setOnClickListener {
            viewModel.performLogout()
        }

        binding.layoutEditName.setOnClickListener {
            showEditNameDialog()
        }

        binding.layoutChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.ivEditProfilePicture.setOnClickListener {
            showImageSourceOptions()
        }

        binding.layoutLanguage.setOnClickListener {
            showLanguageChangeDialog()
        }

        binding.layoutTheme.setOnClickListener {
            showThemeChangeDialog()
        }
    }

    private fun updateProfileUI(profile: ProfileResponse) {
        binding.apply {
            textUsername.text = profile.name
            tvShowEmail.text = profile.email

            val savedAvatarUrl = preferencesManager.getProfileImageUrl()

            if (!savedAvatarUrl.isNullOrEmpty()) {
                Glide.with(this@UserActivity)
                    .load(savedAvatarUrl)
                    .placeholder(R.drawable.ic_avatar_outlined_250)
                    .error(R.drawable.ic_avatar_outlined_250)
                    .into(ivProfilePicture)
            }
        }
    }

    private fun showEditNameDialog() {
        showLoadingDialog(getString(R.string.loading_data))

        viewModel.fetchProfile()

        viewModel.profileResult.observe(this, object : Observer<Result<Any>?> {
            override fun onChanged(result: Result<Any>?) {
                when (result) {
                    is Result.Success -> {
                        viewModel.profileResult.removeObserver(this)

                        dismissLoadingDialog()

                        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
                        val etName = dialogView.findViewById<EditText>(R.id.etEditName)
                        val btnSave = dialogView.findViewById<TextView>(R.id.btnSaveName)
                        val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancel)

                        val profile = result.data as ProfileResponse
                        etName.setText(profile.name)

                        val dialog = AlertDialog.Builder(this@UserActivity)
                            .setView(dialogView)
                            .setCancelable(true)
                            .create()

                        dialog.setOnShowListener {
                            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
                        }

                        btnSave.setOnClickListener {
                            val newName = etName.text.toString().trim()

                            if (newName.isEmpty()) {
                                etName.error = getString(R.string.name_empty_error)
                                return@setOnClickListener
                            }

                            if (newName.length < 3) {
                                etName.error = getString(R.string.name_length_error)
                                return@setOnClickListener
                            }

                            viewModel.changeName(newName)
                            dialog.dismiss()
                        }

                        tvCancel.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.show()
                    }

                    is Result.Error -> {
                        dismissLoadingDialog()

                        showCustomErrorDialog(result.message)
                    }

                    is Result.Loading -> {
                        //
                    }

                    null -> {
                        dismissLoadingDialog()
                    }
                }
            }
        })
    }

    private fun uploadAvatar(file: File) {
        viewModel.uploadAvatar(file)
    }

    private fun showLanguageChangeDialog() {
        val languages = arrayOf(
            getString(R.string.system_default),
            getString(R.string.indonesia), getString(R.string.english)
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_language))

        val dialogView = layoutInflater.inflate(R.layout.dialog_list, null)
        val listView: ListView = dialogView.findViewById(R.id.listViewDialog)

        val adapter = LanguageAdapter(this, languages)
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
                0 -> preferencesManager.setSystemDefaultLanguage()
                1 -> preferencesManager.setIndonesianLanguage()
                2 -> preferencesManager.setEnglishLanguage()
            }
            dialog.dismiss()
            Handler(Looper.getMainLooper()).post {
                showCustomAlertDialog(getString(R.string.language_change_success)) {
                    recreateWithTransition()
                }
            }
        }

        dialog.show()
    }

    private fun showThemeChangeDialog() {
        val themes = arrayOf(
            getString(R.string.system_default), getString(R.string.light),
            getString(
                R.string.dark
            )
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_theme))

        val dialogView = layoutInflater.inflate(R.layout.dialog_list, null)
        val listView: ListView = dialogView.findViewById(R.id.listViewDialog)

        val adapter = ThemeAdapter(this, themes)
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
                0 -> {
                    preferencesManager.saveTheme(AppTheme.SYSTEM)
                }

                1 -> {
                    preferencesManager.saveTheme(AppTheme.LIGHT)
                }

                2 -> {
                    preferencesManager.saveTheme(AppTheme.DARK)
                }
            }
            dialog.dismiss()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    this,
                    getString(R.string.theme_change_success),
                    Toast.LENGTH_SHORT
                ).show()
                recreateWithTransition()
            }
        }

        dialog.show()
    }

    private fun showCustomErrorDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_crossmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_error_title)
        titleTextView.text = message

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
            dialog.setCanceledOnTouchOutside(true)
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showImageSourceOptions() {
        imagePickerHelper.showImageSourceOptions(context)
    }

    private fun setupNewsNotificationSwitch() {
        switchNewsNotification.isChecked = preferencesManager.isNewsNotificationEnabled()

        switchNewsNotification.setOnCheckedChangeListener { _, isChecked ->
            showNewsNotificationConfirmationDialog(isChecked)
        }
    }

    private fun showNewsNotificationConfirmationDialog(isChecked: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showCustomAlertDialog(getString(R.string.news_notification_enabled_success))
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showNotificationPermissionRationaleDialog(isChecked)
                }

                else -> {
                    requestNotificationPermission(isChecked)
                }
            }
        } else {
            showCustomErrorDialog(getString(R.string.news_notification_disabled_success))
        }
    }

    private fun showNotificationPermissionRationaleDialog(isChecked: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.notification_permission_title)
            .setMessage(R.string.notification_permission_rationale)
            .setPositiveButton(R.string.yes) { _, _ ->
                requestNotificationPermission(isChecked)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                switchNewsNotification.isChecked = !isChecked
            }
            .create()

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

    private fun requestNotificationPermission(isChecked: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    showCustomAlertDialog(getString(R.string.news_notification_enabled_success))
                } else {
                    switchNewsNotification.isChecked = false
                    showCustomErrorDialog(getString(R.string.notification_permission_denied))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePickerHelper.handleActivityResult(context, requestCode, resultCode, data)
    }

    private fun setupAboutDeveloperListener() {
        binding.layoutAboutDeveloper.setOnClickListener {
            val intent = Intent(this, AboutDeveloperActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupHistoryScanListener() {
        binding.layoutHistoryScan.setOnClickListener {
            val intent = Intent(this, HistoryScanActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupPrivacyPolicyListener() {
        binding.layoutPrivacyPolicy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
    }
}