package com.bangkit.batikloka.ui.auth.login

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
import com.bangkit.batikloka.databinding.ActivityLoginBinding
import com.bangkit.batikloka.ui.auth.codeverification.VerificationActivity
import com.bangkit.batikloka.ui.auth.emailverification.EmailVerificationActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity
import com.bangkit.batikloka.ui.auth.viewmodel.AuthViewModelFactory
import com.bangkit.batikloka.ui.main.MainActivity
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var preferencesManager: PreferencesManager
    private var loadingDialog: ProgressDialog? = null

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_FROM_LOGIN = "extra_from_login"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()

        if (isUserAlreadyLoggedIn()) {
            navigateToMainActivity()
            return
        }

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
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
    }

    private fun isUserAlreadyLoggedIn(): Boolean {
        return viewModel.authRepository.isUserLoggedIn()
    }

    private fun setupBinding() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Result.Success -> handleSuccessResult(result)
                is Result.Error -> handleErrorResult(result)
                is Result.Loading -> handleLoadingResult()
                null -> resetButtonState()
            }
        }
    }

    private fun handleSuccessResult(result: Result.Success<*>) {
        dismissLoadingDialog()
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.login)

        showCustomAlertDialog(result.data.toString()) {
            navigateToMainActivity()
        }
    }

    private fun handleErrorResult(result: Result.Error) {
        dismissLoadingDialog()
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.login)

        when {
            result.message.contains("not verified", ignoreCase = true) -> {
                handleUnverifiedUser(binding.etEmail.text.toString().trim())
            }

            result.message.contains("not found", ignoreCase = true) -> {
                handleUserNotFound()
            }

            else -> {
                showCustomErrorDialog(result.message)
            }
        }
    }

    private fun handleLoadingResult() {
        showLoadingDialog(getString(R.string.logging_in))
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = getString(R.string.logging_in)
    }

    private fun resetButtonState() {
        dismissLoadingDialog()
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.login)
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

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvForgotPassword.setOnClickListener {
            navigateToEmailVerificationActivity()
        }

        binding.tvLogin.setOnClickListener {
            navigateToRegister()
        }

        binding.ivShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        viewModel.login(email, password)
    }

    private fun handleUnverifiedUser(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.verification_required_title))
            .setMessage(getString(R.string.unverified_account_message))
            .setPositiveButton(getString(R.string.verify_now)) { _, _ ->
                navigateToVerification(email)
            }
            .setNegativeButton(getString(R.string.cancel), null)

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

    private fun handleUserNotFound() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.user_not_found_title))
            .setMessage(getString(R.string.user_not_found_message))
            .setPositiveButton(getString(R.string.register_now)) { _, _ ->
                navigateToRegister()
            }
            .setNegativeButton(getString(R.string.cancel), null)

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

    private fun navigateToVerification(email: String) {
        val intent = Intent(this, VerificationActivity::class.java).apply {
            putExtra(EXTRA_EMAIL, email)
            putExtra(EXTRA_FROM_LOGIN, true)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun navigateToEmailVerificationActivity() {
        val intent = Intent(this, EmailVerificationActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
