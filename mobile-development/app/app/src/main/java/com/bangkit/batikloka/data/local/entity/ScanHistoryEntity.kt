package com.bangkit.batikloka.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val description: String,
    val probability: Double,
    val imagePath: String,
    val scanDate: Date = Date()
)