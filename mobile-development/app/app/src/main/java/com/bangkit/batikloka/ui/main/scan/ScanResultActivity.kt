package com.bangkit.batikloka.ui.main.scan

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.BatikDatabase
import com.bangkit.batikloka.data.remote.api.ApiConfig
import com.bangkit.batikloka.data.remote.response.PredictResponse
import com.bangkit.batikloka.data.repository.PredictRepository
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import com.bangkit.batikloka.databinding.ActivityScanResultBinding
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.ScanResult
import com.bumptech.glide.Glide
import java.io.File

class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanResultBinding
    private var loadingDialog: ProgressDialog? = null

    companion object {
        private const val TAG = "ScanResultActivity"
    }

    private val viewModel: ScanResultViewModel by viewModels {
        ScanViewModelFactory(
            PredictRepository(
                ApiConfig.getPredictApiService(this, PreferencesManager(this))
            ),
            ScanHistoryRepository(
                this,
                BatikDatabase.getDatabase(this).scanHistoryDao()
            ),
            PreferencesManager(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        observeViewModel()
        processImageUri()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun observeViewModel() {
        viewModel.predictResult.observe(this) { scanResult ->
            when (scanResult) {
                is ScanResult.Loading -> {
                    showLoadingDialog(getString(R.string.processing_images))
                }

                is ScanResult.Success -> {
                    dismissLoadingDialog()
                    handleSuccessResult(scanResult.data)
                }

                is ScanResult.Error -> {
                    dismissLoadingDialog()
                    showScanFailedDialog()
                }

                else -> {

                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun handleSuccessResult(predictResponse: PredictResponse) {
        binding.tvLabel.text = predictResponse.label
        binding.tvShortDescription.text = predictResponse.description

        val highestProbability = predictResponse.predictions.maxOrNull() ?: 0.0
        binding.tvPrediction.text = String.format(
            getString(R.string.probability),
            highestProbability * 100
        )

        val imageUriString = intent.getStringExtra("IMAGE_URI")
        imageUriString?.let { uriString ->
            val imageUri = Uri.parse(uriString)
            val imageFile = getFileFromUri(imageUri)

            imageFile?.let { file ->
                saveScanHistory(predictResponse, file, highestProbability)
            }
        }

        showCustomSuccessDialog(getString(R.string.batik_successfully_identified))
    }

    private fun saveScanHistory(
        predictResponse: PredictResponse,
        imageFile: File,
        probability: Double
    ) {
        viewModel.saveScanHistory(predictResponse, imageFile)
    }

    private fun processImageUri() {
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        imageUriString?.let { uriString ->
            val imageUri = Uri.parse(uriString)

            Glide.with(this)
                .load(imageUri)
                .into(binding.ivScanImage)

            val imageFile = getFileFromUri(imageUri)
            imageFile?.let { file ->
                viewModel.predictBatik(file)
            } ?: run {
                showCustomErrorDialog(getString(R.string.failed_to_process_image))
            }
        } ?: run {
            showCustomErrorDialog(getString(R.string.no_image_selected))
            finish()
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("batik_image", ".jpg", cacheDir)
            tempFile.deleteOnExit()

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showLoadingDialog(message: String) {
        loadingDialog?.dismiss()
        loadingDialog = ProgressDialog(this).apply {
            setMessage(message)
            setCancelable(false)
            setOnShowListener {
                window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
            }
            show()
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun showScanFailedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.scan_failed_title))
            .setMessage(getString(R.string.scan_failed_message))
            .setPositiveButton(getString(R.string.try_again)) { _, _ ->
                val intent = Intent(this, ScanActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)

        val dialog = builder.create()
        dialog.setOnShowListener { dialogInterface ->
            val alertDialog = dialogInterface as AlertDialog
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.caramel_gold))

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

    private fun showCustomSuccessDialog(title: String, onDismiss: () -> Unit = {}) {
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
}