package com.bangkit.batikloka.data.local.entity

import androidx.annotation.DrawableRes

data class TourItem(
    @DrawableRes val imageResId: Int,
    val titleText: String,
    val descriptionText: String,
)