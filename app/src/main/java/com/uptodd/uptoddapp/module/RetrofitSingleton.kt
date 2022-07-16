package com.uptodd.uptoddapp.module

import com.uptodd.uptoddapp.utils.FilesUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitSingleton {

    private val httpInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }


    companion object {
        @Volatile
        private var INSTANCE: RetrofitSingleton? = null

        fun getInstance(): RetrofitSingleton {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = RetrofitSingleton()
                }
                return INSTANCE!!
            }
        }

    }


    private val client = OkHttpClient.Builder().apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
            /*.addInterceptor {
                val operation = it.request().newBuilder()
                    .addHeader("Authorization", auth)
                    .addHeader("Content-Type", "application/xml")
                    .build()
                it.proceed(operation)
            }*/
            .addInterceptor(httpInterceptor)
    }.build()

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(FilesUtils.BASE_URL)
            .build()
    }


}