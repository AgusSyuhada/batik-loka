package com.bangkit.batikloka.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Developer(
    val image: String,
    val name: String,
    val age: Int,
    val major: String,
    val domicile: String,
    val learningPath: String,
    val university: String,
    val description: String,
    val instagram: String? = null,
    val linkedin: String? = null,
    val github: String? = null,
) : Parcelable