package com.bangkit.batikloka.data.remote.response

import com.google.gson.annotations.SerializedName

data class PredictResponse(
    @SerializedName("predictions")
    val predictions: List<Double>,

    @SerializedName("description")
    val description: String,

    @SerializedName("label")
    val label: String
)