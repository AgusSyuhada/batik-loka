package com.bangkit.batikloka.data.remote.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class NewsResponse(
    val data: List<NewsItem>,
    val length: Int,
    val status: Int
)

@Parcelize
data class NewsItem(
    val body: String?,
    val gambar: String?,
    val judul: String?,
    val link: String? = null,
    val waktu: String?
) : Parcelable