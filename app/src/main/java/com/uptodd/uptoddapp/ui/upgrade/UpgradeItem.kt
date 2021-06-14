package com.uptodd.uptoddapp.ui.upgrade

import com.google.gson.annotations.SerializedName

class UpgradeItem {

    @SerializedName("id")
    var id:Int=-1

    @SerializedName("stage")
    var stage=""

    @SerializedName("country")
    var country=""

    @SerializedName("productMonth")
    var productMonth=0

    @SerializedName("emiAmount")
    var emiAmount=0

    @SerializedName("discount")
    var discount=0

    @SerializedName("originialPrice")
    var original=0
    @SerializedName("amountToBePaid")
    var amountToBePaid=0
    @SerializedName("programName")
    var programName=""
    @SerializedName("keyFeatures")
    var keyFeatures:ArrayList<String>?=null
}