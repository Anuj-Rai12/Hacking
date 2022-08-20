package com.uptodd.uptoddapp.datamodel.forgetpass


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Data(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("otp") val otp: Int,
    @SerializedName("status") val status: String
):Parcelable