package com.bangkit.batikloka.ui.auth.emailverification

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.databinding.ActivityEmailVerificationBinding
import com.bangkit.batikloka.ui.auth.createnewpassword.CreateNewPasswordActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity
import com.bangkit.batikloka.ui.auth.viewmodel.AuthViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var viewModel: EmailVerificationViewModel
    private lateinit var preferencesManager: PreferencesManager
    private var loadingDialog: ProgressDialog? = null

    companion object {
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_FROM_EMAIL_VERIFY = "extra_from_verify_email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()
        setupBinding()
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViewModel() {
        val context = this
        preferencesManager = PreferencesManager(this)
        val authApiService = ApiConfig.getAuthApiService(this, preferencesManager)
        val authRepository = AuthRepository(authApiService, preferencesManager)

        val viewModelFactory = AuthViewModelFactory(authRepository, context)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[EmailVerificationViewModel::class.java]
    }

    private fun setupBinding() {
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupObservers() {
        viewModel.forgetPasswordResult.observe(this) { result ->
            result?.let {
                when (it) {
                    is Result.Success -> handleSuccessResult()
                    is Result.Error -> handleErrorResult(it)
                    is Result.Loading -> handleLoadingResult()
                    else -> resetButtonState()
                }
            } ?: resetButtonState()
        }
    }

    private fun handleSuccessResult() {
        dismissLoadingDialog()
        binding.btnVerifyEmail.isEnabled = true
        binding.btnVerifyEmail.text = getString(R.string.btn_send)

        showCustomAlertDialog(getString(R.string.dialog_verification_code_sent)) {
            navigateToCreateNewPassword(binding.etEmail.text.toString().trim())
        }
    }

    private fun handleErrorResult(result: Result.Error) {
        dismissLoadingDialog()
        binding.btnVerifyEmail.isEnabled = true
        binding.btnVerifyEmail.text = getString(R.string.btn_send)

        when {
            result.error == "User not found" -> {
                handleUserNotFound()
            }

            else -> {
                showCustomErrorDialog(result.message)
            }
        }
    }

    private fun handleLoadingResult() {
        showLoadingDialog(getString(R.string.btn_sending))
        binding.btnVerifyEmail.isEnabled = false
        binding.btnVerifyEmail.text = getString(R.string.btn_sending)
    }

    private fun resetButtonState() {
        dismissLoadingDialog()
        binding.btnVerifyEmail.isEnabled = true
        binding.btnVerifyEmail.text = getString(R.string.btn_send)
    }

    private fun setupClickListeners() {
        binding.btnVerifyEmail.setOnClickListener {
            viewModel.resetState()

            val email = binding.etEmail.text.toString().trim()
            viewModel.forgetPassword(email)
        }
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

    private fun handleUserNotFound() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_email_not_found_title))
            .setMessage(getString(R.string.dialog_email_not_found_message))
            .setPositiveButton(getString(R.string.btn_register_now)) { _, _ ->
                navigateToRegister()
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

    override fun onBackPressed() {
        preferencesManager.clearResetPasswordData()
        super.onBackPressed()
    }

    private fun navigateToCreateNewPassword(email: String) {
        val intent = Intent(this, CreateNewPasswordActivity::class.java).apply {
            putExtra(EXTRA_EMAIL, email)
            putExtra(EXTRA_FROM_EMAIL_VERIFY, true)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}