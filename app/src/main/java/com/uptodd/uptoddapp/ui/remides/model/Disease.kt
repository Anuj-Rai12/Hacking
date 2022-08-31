package com.uptodd.uptoddapp.ui.remides.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Disease(
    @SerializedName("definition") val definition: String,
    @SerializedName("id") val id: Int,
    @SerializedName("link") val link: String,
    @SerializedName("max_age") val maxAge: Int,
    @SerializedName("min_age") val minAge: Int,
    @SerializedName("name") val name: String,
    @SerializedName("remedies") val remedies: String,
    @SerializedName("symptoms") val symptoms: String
):Parcelable