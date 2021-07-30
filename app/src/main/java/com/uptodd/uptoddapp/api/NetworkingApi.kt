package com.uptodd.uptoddapp.api

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.uptodd.uptoddapp.utilities.AllUtil

class NetworkingApi {

companion object
{
    fun AndroidNetworking.get(url:String): ANRequest.GetRequestBuilder<out ANRequest.GetRequestBuilder<*>>? {

        return AndroidNetworking.get(url).addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
    }
    fun AndroidNetworking.postS(url:String): ANRequest.PostRequestBuilder<out ANRequest.PostRequestBuilder<*>>? {

        return AndroidNetworking.post(url).addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
    }
    fun AndroidNetworking.put(url:String): ANRequest.PostRequestBuilder<out ANRequest.PostRequestBuilder<*>>? {

        return AndroidNetworking.put(url).addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
    }
}
}