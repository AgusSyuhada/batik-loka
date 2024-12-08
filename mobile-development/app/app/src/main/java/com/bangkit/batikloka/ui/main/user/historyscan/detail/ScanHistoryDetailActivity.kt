package com.bangkit.batikloka.ui.main.user.historyscan.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.BatikDatabase
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import com.bangkit.batikloka.databinding.ActivityScanHistoryDetailBinding
import com.bangkit.batikloka.ui.main.user.historyscan.viewmodel.HistoryViewModelFactory
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ScanHistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanHistoryDetailBinding
    private val viewModel: ScanHistoryDetailViewModel by viewModels {
        HistoryViewModelFactory(
            ScanHistoryRepository(
                this,
                BatikDatabase.getDatabase(this).scanHistoryDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val scanHistoryId = intent.getIntExtra("SCAN_HISTORY_ID", -1)

        observeScanHistoryDetail(scanHistoryId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun observeScanHistoryDetail(id: Int) {
        if (id == -1) {
            showCustomErrorDialog(getString(R.string.data_not_found))
            finish()
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getScanHistoryById(id).collect { scanHistory ->
                    scanHistory?.let { history ->
                        bindScanHistoryData(history)
                    } ?: run {
                        showCustomErrorDialog(getString(R.string.data_not_found))
                        finish()
                    }
                }
            }
        }
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

    @SuppressLint("DefaultLocale")
    private fun bindScanHistoryData(scanHistory: ScanHistoryEntity) {
        with(binding) {
            tvLabel.text = scanHistory.label

            tvPrediction.text = String.format(
                getString(R.string.probability),
                scanHistory.probability * 100
            )

            tvDate.text = SimpleDateFormat(
                "dd MMMM yyyy HH:mm",
                Locale.getDefault()
            ).format(scanHistory.scanDate)

            Glide.with(this@ScanHistoryDetailActivity)
                .load(File(scanHistory.imagePath))
                .into(ivScanImage)

            tvShortDescription.text = scanHistory.description
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
