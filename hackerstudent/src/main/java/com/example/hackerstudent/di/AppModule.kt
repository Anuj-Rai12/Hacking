package com.example.hackerstudent.di

import com.example.hackerstudent.api.RestApi
import com.example.hackerstudent.utils.GetConstStringObj
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun getActionSetting() = ActionCodeSettings.newBuilder()
        .setUrl(GetConstStringObj.EMAIL_VERIFICATION_LINK)
        .setHandleCodeInApp(true)
        .setAndroidPackageName("com.example.hackerstudent", false, null)
        .build()

    private val okHttpClient = okhttp3.OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS).build()

    @Singleton
    @Provides
    fun getRestApiServcice() = Retrofit.Builder().baseUrl(GetConstStringObj.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()).build().create(RestApi::class.java)

    @Singleton
    @Provides
    fun fireStore() = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun getQuery(fireStore: FirebaseFirestore) =
        fireStore.collection(GetConstStringObj.Create_course)
            .limit(GetConstStringObj.Per_page.toLong())

}