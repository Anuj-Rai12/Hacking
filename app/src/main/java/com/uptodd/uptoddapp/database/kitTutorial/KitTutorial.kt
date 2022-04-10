package com.uptodd.uptoddapp.database.kitTutorial

import com.google.gson.annotations.SerializedName
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import java.io.Serializable

data class KitTutorial(@SerializedName("categoryName")var category:String?,
                       @SerializedName("image") var image:String?,
                       @SerializedName("tutorials") var tutorials:ArrayList<ActivitySample>
                       ):Serializable