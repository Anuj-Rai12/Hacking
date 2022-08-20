package com.uptodd.uptoddapp.datamodel.forgetpass


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ForgetPassResponse(
    @SerializedName("data") val data: Data,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String
):Parcelable
