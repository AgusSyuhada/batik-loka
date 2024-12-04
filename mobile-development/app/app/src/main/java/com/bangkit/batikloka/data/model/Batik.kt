package com.bangkit.batikloka.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Batik(
    val name: String,
    val image: String,
    val description: String,
    val category: String,
) : Parcelable