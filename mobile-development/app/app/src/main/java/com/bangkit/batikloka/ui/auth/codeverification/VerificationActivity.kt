package com.bangkit.batikloka.ui.auth.codeverification

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.databinding.ActivityVerificationBinding
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.viewmodel.AuthViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result

class VerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerificationBinding
    private lateinit var viewModel: VerificationViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var preferencesManager: PreferencesManager
    private var loadingDialog: ProgressDialog? = null

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_REGISTRATION_STEP = "extra_registration_step"
        const val EXTRA_FROM_LOGIN = "extra_from_login"

        fun newIntent(
            context: Context, email: String, registrationStep: String? = null
        ): Intent {
            return Intent(context, VerificationActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
                registrationStep?.let {
                    putExtra(EXTRA_REGISTRATION_STEP, it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()
        setupBinding()
        setupUI()
        setupObservers()
    }

    private fun initializeViewModel() {
        val context = this
        preferencesManager = PreferencesManager(this)
        val authApiService = ApiConfig.getAuthApiService(this, preferencesManager)
        authRepository = AuthRepository(authApiService, preferencesManager)

        val viewModelFactory = AuthViewModelFactory(authRepository, context)
        viewModel = ViewModelProvider(this, viewModelFactory)[VerificationViewModel::class.java]
    }

    private fun setupBinding() {
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupUI() {
        val email = intent.getStringExtra(EXTRA_EMAIL)
        val registrationStep = intent.getStringExtra(EXTRA_REGISTRATION_STEP)

        if (email.isNullOrEmpty()) {
            showCustomErrorDialog(getString(R.string.email_required))
            finish()
            return
        }

        val subtitle = getString(R.string.verification_subtitle).plus(" $email")
        binding.tvVerificationSubtitle.text = subtitle

        setupListeners(email, registrationStep)
    }

    private fun setupObservers() {
        viewModel.verifyOtpResult.observe(this) { result ->
            when (result) {
                is Result.Success -> handleSuccessResult(result)
                is Result.Error -> handleErrorResult(result)
                is Result.Loading -> handleLoadingResult()
                else -> resetButtonState()
            }
        }
    }

    private fun handleSuccessResult(result: Result.Success<*>) {
        dismissLoadingDialog()
        binding.btnVerifyAccount.isEnabled = true
        binding.btnVerifyAccount.text = getString(R.string.verification)

        showCustomAlertDialog(result.data.toString()) {
            navigateToNextScreen()
        }
    }

    private fun handleErrorResult(result: Result.Error) {
        dismissLoadingDialog()
        binding.btnVerifyAccount.isEnabled = true
        binding.btnVerifyAccount.text = getString(R.string.verification)

        showCustomErrorDialog(result.message)
    }

    private fun handleLoadingResult() {
        showLoadingDialog(getString(R.string.verifying))
        binding.btnVerifyAccount.isEnabled = false
        binding.btnVerifyAccount.text = getString(R.string.verifying)
    }

    private fun resetButtonState() {
        dismissLoadingDialog()
        binding.btnVerifyAccount.isEnabled = true
        binding.btnVerifyAccount.text = getString(R.string.verification)
    }

    private fun setupListeners(email: String, registrationStep: String?) {
        binding.btnVerifyAccount.setOnClickListener {
            viewModel.resetState()
            performVerifyOtp(email, registrationStep)
        }
    }

    private fun performVerifyOtp(email: String, registrationStep: String?) {
        viewModel.verifyOtp(email, binding.etVerification.text.toString().trim())
    }

    private fun navigateToNextScreen() {
        val registrationStep = intent.getStringExtra(EXTRA_REGISTRATION_STEP)

        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(EXTRA_REGISTRATION_STEP, registrationStep)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
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

    private fun showCustomErrorDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_crossmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_error_title)
        titleTextView.text = message

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()

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

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()

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
}