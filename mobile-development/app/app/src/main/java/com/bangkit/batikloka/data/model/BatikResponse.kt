package com.bangkit.batikloka.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatikResponse(
    val batik: List<Batik>,
) : Parcelable