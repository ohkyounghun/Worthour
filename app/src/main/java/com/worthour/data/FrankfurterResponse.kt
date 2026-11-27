package com.worthour.data

import com.google.gson.annotations.SerializedName

data class FrankfurterResponse(
    @SerializedName("date") val date: String,
    @SerializedName("base") val base: String,
    @SerializedName("quote") val quote: String,
    @SerializedName("rate") val rate: Double
)
