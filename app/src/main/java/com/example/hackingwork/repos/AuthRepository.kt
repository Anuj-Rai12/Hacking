package com.example.hackingwork.repos

import com.example.hackingwork.utils.MySealed
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor() {
    private val authInstance by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUser by lazy {
        authInstance.currentUser
    }

    private val actionCodeString = ActionCodeSettings.newBuilder()
        .setUrl("https://hackerstudent.verify.com/VerifyEmail")
        .setHandleCodeInApp(true)
        .setAndroidPackageName("com.example.hackingwork", false, null)
        .build()

    fun sendEmailLinkWithToVerify(email: String) = flow {
        emit(MySealed.Loading("Verification Link is been Sending"))
        val data = try {
            authInstance.sendSignInLinkToEmail(email, actionCodeString).await()
            MySealed.Success(
                "Verification Email is Been Sent at your Given Email address\n$email\n" +
                        "Please Verify It For Further Process."
            )
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

}