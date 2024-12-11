package com.bangkit.batikloka.ui.auth.register

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.databinding.ActivityRegisterBinding
import com.bangkit.batikloka.ui.auth.codeverification.VerificationActivity
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.viewmodel.AuthViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var preferencesManager: PreferencesManager
    private var loadingDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()
        setupBinding()
        setupObservers()
        setupListeners()
        animateLoginItems()
    }

    private fun initializeViewModel() {
        val context = this
        preferencesManager = PreferencesManager(this)
        val authApiService = ApiConfig.getAuthApiService(this, preferencesManager)
        val authRepository = AuthRepository(authApiService, preferencesManager)
        val viewModelFactory = AuthViewModelFactory(authRepository, context)
        viewModel = ViewModelProvider(this, viewModelFactory)[RegisterViewModel::class.java]
    }

    private fun setupBinding() {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is Result.Success -> handleSuccessResult()
                is Result.Error -> handleErrorResult(result)
                is Result.Loading -> handleLoadingResult()
                null -> resetButtonState()
            }
        }
    }

    private fun handleSuccessResult() {
        dismissLoadingDialog()
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = getString(R.string.btn_create_account)

        showCustomAlertDialog(getString(R.string.dialog_registration_success)) {
            navigateToVerification(
                binding.etEmail.text.toString().trim()
            )
        }
    }

    private fun handleErrorResult(result: Result.Error) {
        dismissLoadingDialog()
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = getString(R.string.btn_create_account)

        when {
            result.error.contains("User exists", ignoreCase = true) -> {
                handleUserExist()
            }

            result.error.contains("invalid email", ignoreCase = true) -> {
                binding.etEmail.error = getString(R.string.error_invalid_email)
            }

            result.error.contains("password", ignoreCase = true) -> {
                binding.etPassword.error = result.message
            }

            else -> {
                showCustomErrorDialog(result.message)
            }
        }
    }

    private fun handleLoadingResult() {
        showLoadingDialog(getString(R.string.btn_registering))
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = getString(R.string.btn_registering)
    }

    private fun resetButtonState() {
        dismissLoadingDialog()
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = getString(R.string.btn_create_account)
    }

    private fun showLoadingDialog(message: String) {
        loadingDialog?.dismiss()
        loadingDialog = ProgressDialog(this)
        loadingDialog?.setMessage(message)
        loadingDialog?.setCancelable(false)
        loadingDialog?.setOnShowListener {
            loadingDialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
        }
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun performRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        viewModel.register(name, email, password, confirmPassword)
    }

    private fun resetErrors() {
        binding.etName.error = null
        binding.etEmail.error = null
        binding.etPassword.error = null
        binding.etConfirmPassword.error = null
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            resetErrors()
            performRegister()
        }

        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }

        binding.ivShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.ivShowConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }
    }

    private fun handleUserExist() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.user_exist_title))
            .setMessage(getString(R.string.user_exist_message))
            .setPositiveButton(getString(R.string.btn_login_now)) { _, _ ->
                navigateToLogin()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)

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

    private fun togglePasswordVisibility() {
        val currentTransformationMethod = binding.etPassword.transformationMethod

        binding.etPassword.transformationMethod =
            if (currentTransformationMethod is PasswordTransformationMethod) {
                SingleLineTransformationMethod()
            } else {
                PasswordTransformationMethod()
            }

        val iconResId = if (currentTransformationMethod is PasswordTransformationMethod)
            R.drawable.ic_visibility
        else
            R.drawable.ic_visibility_off

        binding.ivShowPassword.setImageResource(iconResId)

        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun toggleConfirmPasswordVisibility() {
        val currentTransformationMethod = binding.etConfirmPassword.transformationMethod

        binding.etConfirmPassword.transformationMethod =
            if (currentTransformationMethod is PasswordTransformationMethod) {
                SingleLineTransformationMethod()
            } else {
                PasswordTransformationMethod()
            }

        val iconResId = if (currentTransformationMethod is PasswordTransformationMethod)
            R.drawable.ic_visibility
        else
            R.drawable.ic_visibility_off

        binding.ivShowConfirmPassword.setImageResource(iconResId)

        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun navigateToVerification(email: String) {
        val intent = VerificationActivity.newIntent(
            this,
            email,
            registrationStep = "profile_completion"
        )
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun animateLoginItems() {
        val views = listOf(
            binding.constraintLayout2,
            binding.constraintLayout3
        )

        views.forEachIndexed { index, view ->
            view.translationY = 100f
            view.alpha = 0f
            view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(index * 200L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
}