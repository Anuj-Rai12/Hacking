package com.uptodd.uptoddapp.database.logindetails


data class UserInfo(
    val uid: String,
    val userName:String,
    val address:String?=null,
    val isNewUser: Boolean,
    val userType : String = "Normal",
    val email: String,
    val loginMethod: String,
    val kidsDob: String = "",
    val babyName: String = "",
    val babyDOB: Long = -1L,
    val profileImageUrl: String,
    val tokenHeader: String = "",
    val parentType: String = "",
    val isPaid : Boolean,
    val loginTime : Long,
    val token : String,
    val loggedIn : Boolean = false
)