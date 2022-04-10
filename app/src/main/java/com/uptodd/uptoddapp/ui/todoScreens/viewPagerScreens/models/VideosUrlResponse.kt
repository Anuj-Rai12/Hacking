package com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class VideosUrlResponse : Serializable{

    @SerializedName("ACTIVITY_PODCAST")
    var activityPodcast:String?=null
    @SerializedName("SESSION")
    var session:String?=null
    @SerializedName("MUSIC")
    var music:String?=null
    @SerializedName("ROUTINES")
    var routines:String?=null
    @SerializedName("MEMORY_BOOSTER")
    var memoryBooster:String?=null
    @SerializedName("KIT_TUTORIAL")
    var kitTutorial:String?=null
    @SerializedName("SUPPORT")
    var support:String?=null
    @SerializedName("CONSULLING")
    var counselling:String?=null

}