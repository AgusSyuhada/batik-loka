package com.bangkit.batikloka.utils

import androidx.recyclerview.widget.DiffUtil
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity

class HistoryDiffCallback : DiffUtil.ItemCallback<ScanHistoryEntity>() {
    override fun areItemsTheSame(
        oldItem: ScanHistoryEntity,
        newItem: ScanHistoryEntity
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ScanHistoryEntity,
        newItem: ScanHistoryEntity
    ): Boolean = oldItem == newItem
}