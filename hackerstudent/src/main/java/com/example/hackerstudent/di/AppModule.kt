package com.example.hackerstudent.di

import com.example.hackerstudent.utils.GetConstStringObj
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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



    @Singleton
    @Provides
    fun fireStore()=FirebaseFirestore.getInstance()

}