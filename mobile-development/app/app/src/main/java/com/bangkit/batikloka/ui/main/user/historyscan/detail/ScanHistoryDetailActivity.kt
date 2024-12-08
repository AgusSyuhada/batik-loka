package com.bangkit.batikloka.ui.main.user.historyscan.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
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
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getScanHistoryById(id).collect { scanHistory ->
                    scanHistory?.let { history ->
                        bindScanHistoryData(history)
                    } ?: run {
                        Toast.makeText(
                            this@ScanHistoryDetailActivity,
                            "Data tidak ditemukan",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
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
