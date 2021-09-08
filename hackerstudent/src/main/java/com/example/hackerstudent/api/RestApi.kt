package com.example.hackerstudent.api

import com.example.hackerstudent.utils.GetConstStringObj
import retrofit2.http.GET

interface RestApi {
    @GET(GetConstStringObj.Get_End_Point)
    suspend fun getInfo(): Motivation
}