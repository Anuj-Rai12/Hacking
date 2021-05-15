package com.uptodd.uptoddapp.database.refer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="refer_user_table")
data class ReferUser(
    @PrimaryKey(autoGenerate = true)
    var referId:Long=0,

    @ColumnInfo(name = "name")
    var name:String?=null,

    @ColumnInfo(name = "email")
    var email:String?=null,

    @ColumnInfo(name = "phone")
    var phone:String?=null,

    @ColumnInfo(name = "referral_status")
    var referralStatus:String?=null,              //"Success","Pending","Cancelled"

    @ColumnInfo(name = "referral_date")
    var referralDate:Long=0,                       //ddmmyyyy format

    @ColumnInfo(name = "is_paid")
    var isPaid:Boolean=false,

    @ColumnInfo(name = "registration_date")
    var registrationDate:Long=0
)