package com.bangkit.batikloka.ui.auth.createnewpassword

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.databinding.ActivityCreateNewPasswordBinding
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.viewmodel.AuthViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result

class CreateNewPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateNewPasswordBinding
    private lateinit var viewModel: CreateNewPasswordViewModel
    private lateinit var preferencesManager: PreferencesManager
    private var loadingDialog: ProgressDialog? = null

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_FROM_EMAIL_VERIFY = "extra_from_email_verify"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()
        setupBinding()
        setupUI()
    }

    private fun initializeViewModel() {
        val context = this
        preferencesManager = PreferencesManager(this)
        val authApiService = ApiConfig.getAuthApiService(this, preferencesManager)
        val authRepository = AuthRepository(authApiService, preferencesManager)

        val viewModelFactory = AuthViewModelFactory(authRepository, context)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[CreateNewPasswordViewModel::class.java]
    }

    private fun setupBinding() {
        binding = ActivityCreateNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupUI() {
        val email = intent.getStringExtra(EXTRA_EMAIL)
        val fromEmailVerify = intent.getBooleanExtra(EXTRA_FROM_EMAIL_VERIFY, false)

        if (email.isNullOrEmpty()) {
            showErrorDialog(getString(R.string.error_invalid_email))
            finish()
            return
        }

        setupPasswordVisibilityListeners()
        setupObservers(email, fromEmailVerify)
    }

    private fun setupObservers(email: String, fromEmailVerify: Boolean) {
        viewModel.resetPasswordResult.observe(this) { result ->
            result?.let {
                when (it) {
                    is Result.Success -> handleSuccessResult()
                    is Result.Error -> handleErrorResult(it)
                    is Result.Loading -> handleLoadingResult()
                    else -> resetButtonState()
                }
            } ?: resetButtonState()
        }

        setupCreateNewPasswordListener(email, fromEmailVerify)
    }

    private fun handleSuccessResult() {
        dismissLoadingDialog()
        binding.btnCreateNewPassword.isEnabled = true
        binding.btnCreateNewPassword.text = getString(R.string.btn_create_new_password)

        showSuccessDialog()
    }

    private fun handleErrorResult(result: Result.Error) {
        dismissLoadingDialog()
        binding.btnCreateNewPassword.isEnabled = true
        binding.btnCreateNewPassword.text = getString(R.string.btn_create_new_password)

        showErrorDialog(result.message)
    }

    private fun handleLoadingResult() {
        showLoadingDialog(getString(R.string.btn_updating))
        binding.btnCreateNewPassword.isEnabled = false
        binding.btnCreateNewPassword.text = getString(R.string.btn_updating)
    }

    private fun resetButtonState() {
        dismissLoadingDialog()
        binding.btnCreateNewPassword.isEnabled = true
        binding.btnCreateNewPassword.text = getString(R.string.btn_create_new_password)
    }

    private fun setupCreateNewPasswordListener(email: String, fromEmailVerify: Boolean) {
        binding.btnCreateNewPassword.setOnClickListener {
            viewModel.resetState()

            val otp = binding.etVerificationEmail.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmNewPassword.text.toString().trim()

            viewModel.resetPassword(email, otp, newPassword, confirmPassword)
        }
    }

    private fun setupPasswordVisibilityListeners() {
        binding.ivShowNewPassword.setOnClickListener {
            togglePasswordVisibility(binding.etNewPassword, binding.ivShowNewPassword)
        }

        binding.ivShowConfirmNewPassword.setOnClickListener {
            togglePasswordVisibility(binding.etConfirmNewPassword, binding.ivShowConfirmNewPassword)
        }
    }

    private fun togglePasswordVisibility(
        editText: android.widget.EditText,
        imageView: android.widget.ImageView
    ) {
        val currentInputType = editText.inputType
        editText.inputType =
            if (currentInputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }

        val isPasswordVisible = currentInputType != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        imageView.setImageResource(
            if (isPasswordVisible) R.drawable.ic_visibility
            else R.drawable.ic_visibility_off
        )

        editText.setSelection(editText.text?.length ?: 0)
    }

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

    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        titleTextView.text = getString(R.string.dialog_password_created)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
            dialog.setCanceledOnTouchOutside(true)
        }

        dialog.setOnDismissListener {
            navigateToLogin()
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000)
    }

    private fun showErrorDialog(message: String) {
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
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}