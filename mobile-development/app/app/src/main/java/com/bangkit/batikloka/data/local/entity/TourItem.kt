package com.bangkit.batikloka.data.local.entity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TourItem(
    @DrawableRes val imageResId: Int,
    @StringRes val titleText: Int,
    @StringRes val descriptionText: Int,
)