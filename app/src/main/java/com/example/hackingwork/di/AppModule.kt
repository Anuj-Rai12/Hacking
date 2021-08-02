package com.example.hackingwork.di

import com.example.hackingwork.utils.GetConstStringObj
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
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
        .setAndroidPackageName("com.example.hackingwork", false, null)
        .build()


    @Singleton
    @Provides
    fun getAuthInstance()=FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun currentUsers(firebaseAuth: FirebaseAuth)=firebaseAuth.currentUser

    @Singleton
    @Provides
    fun fireStore()=FirebaseFirestore.getInstance()

}