package com.uptodd.uptoddapp.datamodel.upgrade


import com.google.gson.annotations.SerializedName

data class UpgradeRequest(
    @SerializedName("id") val id: Long
)