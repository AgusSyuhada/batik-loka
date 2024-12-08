package com.bangkit.batikloka.ui.main.user.historyscan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.BatikDatabase
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import com.bangkit.batikloka.databinding.ActivityHistoryScanBinding
import com.bangkit.batikloka.ui.adapter.HistoryScanAdapter
import com.bangkit.batikloka.ui.main.user.historyscan.detail.ScanHistoryDetailActivity
import com.bangkit.batikloka.ui.main.user.historyscan.viewmodel.HistoryViewModelFactory
import kotlinx.coroutines.launch

class HistoryScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryScanBinding
    private lateinit var historyScanAdapter: HistoryScanAdapter

    private val viewModel: HistoryScanViewModel by viewModels {
        HistoryViewModelFactory(
            ScanHistoryRepository(
                this,
                BatikDatabase.getDatabase(this).scanHistoryDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupFabDeleteAll()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    private fun setupRecyclerView() {
        historyScanAdapter = HistoryScanAdapter(
            context = this,
            onDeleteClick = { scanHistory ->
                showDeleteSingleHistoryDialog(scanHistory)
            },
            onItemClick = { scanHistory ->
                navigateToScanHistoryDetail(scanHistory)
            }
        )

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryScanActivity)
            adapter = historyScanAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scanHistory.collect { histories ->
                    historyScanAdapter.submitList(histories)

                    binding.textViewNoHistory.visibility =
                        if (histories.isEmpty()) View.VISIBLE
                        else View.GONE
                }
            }
        }
    }

    private fun setupFabDeleteAll() {
        binding.fabDeleteAll.setOnClickListener {
            showDeleteAllHistoryDialog()
        }
    }

    private fun showDeleteAllHistoryDialog() {
        showDeleteConfirmationDialog(
            getString(R.string.sure_delete_all_history),
            onConfirm = {
                viewModel.deleteAllHistory()
            }
        )
    }

    private fun showDeleteSingleHistoryDialog(scanHistory: ScanHistoryEntity) {
        showDeleteConfirmationDialog(
            getString(R.string.sure_delete_history, scanHistory.label),
            onConfirm = {
                viewModel.deleteSingleHistory(scanHistory)
            }
        )
    }

    private fun showDeleteConfirmationDialog(message: String, onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.confirm_delete))
            .setMessage(message)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                onConfirm()
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

    private fun navigateToScanHistoryDetail(scanHistory: ScanHistoryEntity) {
        val intent = Intent(this, ScanHistoryDetailActivity::class.java).apply {
            putExtra("SCAN_HISTORY_ID", scanHistory.id)
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}