package com.uptodd.uptoddapp.database.logindetails

data class DoctorLoginInfo(
    val uid: String,
    val email: String,
    val userType: String,
    val token: String,
    val loggedIn: Boolean
)