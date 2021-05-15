package com.uptodd.uptoddapp.database.logindetails

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="explorers")
data class Explorers(
    @PrimaryKey(autoGenerate = true)
    var key:Long=0,

    @ColumnInfo(name="uid")
    var uid:String?=null,

    @ColumnInfo(name="login_method")
    var loginMethod:String?=null,                   //"email"  "google"  "facebook"

    @ColumnInfo(name="email")
    var email:String?=null,

    @ColumnInfo(name="phone")
    var phone:String?=null,

    @ColumnInfo(name="name")
    var name:String?=null,

    @ColumnInfo(name="profile_image_url")
    var profileImageUrl:String?=null,

    @ColumnInfo(name="is_new_user")
    var isNewUser:Boolean=false
)