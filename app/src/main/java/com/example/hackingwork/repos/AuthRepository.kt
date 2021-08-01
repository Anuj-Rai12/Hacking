package com.example.hackingwork.repos

import com.example.hackingwork.utils.CreateUserAccount
import com.example.hackingwork.utils.GetConstStringObj
import com.example.hackingwork.utils.MySealed
import com.example.hackingwork.utils.UserStore
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
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
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
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
                        "Please Verify It For Further Process.\n" +
                        "Tips\n" +
                        "Check Spam Section of Email For Verification."
            )
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun createInWithEmail(email: String, link: String) = flow {
        emit(MySealed.Loading("Create User Profile."))
        val data = try {
            authInstance.signInWithEmailLink(email, link).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun updatePhoneNumber(credential: PhoneAuthCredential, password: String) = flow {
        emit(MySealed.Loading("Checking Otp ..."))
        val data = try {
            currentUser?.updatePhoneNumber(credential)?.await()
            currentUser?.updatePassword(password)?.await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun createUserAccount(userStore: UserStore) = flow {
        emit(MySealed.Loading("Creating User Profile"))
        val data = try {
            val createUserAccount = CreateUserAccount(
                email = userStore.email,
                phone = userStore.phone,
                firstname = userStore.firstname,
                lastname = userStore.lastname,
                ipaddress = userStore.ipAddress,
            )
            fireStore.collection(GetConstStringObj.USERS).document(currentUser?.uid!!)
                .set(createUserAccount).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

}