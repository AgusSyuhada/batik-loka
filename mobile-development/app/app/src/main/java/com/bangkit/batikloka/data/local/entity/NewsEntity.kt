package com.bangkit.batikloka.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_table")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val body: String?,
    val gambar: String?,
    val judul: String?,
    val link: String? = null,
    val waktu: String?
)