package ru.practicum.android.diploma.network.dto.linked

import com.google.gson.annotations.SerializedName

class LogoUrls(
    @SerializedName("90") val size90: String,
    @SerializedName("240") val size240: String,
    @SerializedName("original") val raw: String,
)
