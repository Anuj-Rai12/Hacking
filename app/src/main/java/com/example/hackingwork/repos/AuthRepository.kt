package com.example.hackingwork.repos

import android.util.Log
import com.example.hackingwork.TAG
import com.example.hackingwork.utils.*
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authInstance: FirebaseAuth,
    private val actionCodeSettings: ActionCodeSettings,
    private val fireStore: FirebaseFirestore
) {
    private val userUdi by lazy {
        FirebaseAuth.getInstance().currentUser
    }

    fun sendEmailLinkWithToVerify(email: String) = flow {
        emit(MySealed.Loading("Verification Link is been Sending"))
        val data = try {
            authInstance.sendSignInLinkToEmail(email, actionCodeSettings).await()
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
        emit(MySealed.Loading("User account is been creating.."))
        val data = try {
            authInstance.signInWithEmailLink(email, link).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun updatePhoneNumber(credential: PhoneAuthCredential, password: String) = flow {
        emit(MySealed.Loading("Checking OTP ..."))
        Log.i(TAG, "updatePhoneNumber: password And Phone Number is Updating ")
        val data = try {
            userUdi?.updatePhoneNumber(credential)?.await()
            userUdi?.updatePassword(password)?.await()
            Log.i(TAG, "updatePhoneNumber: Password is Updated")
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun createUserAccount(userStore: UserStore) = flow {
        emit(MySealed.Loading("Creating User Profile.."))
        val data = try {
            val createUserAccount = CreateUserAccount(
                email = userStore.email,
                phone = userStore.phone,
                firstname = userStore.firstname,
                lastname = userStore.lastname,
                ipaddress = userStore.ipAddress,
                password = userStore.password
            )
            Log.i(TAG, "createUserAccount: User Id -> $userUdi")
            fireStore.collection(GetConstStringObj.USERS).document(userUdi?.uid!!)
                .set(createUserAccount).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun checkEmailOfUsers(email: String, password: String) = flow {
        emit(MySealed.Loading("Checking Users Account.."))
        val data = try {
            authInstance.signInWithEmailAndPassword(email, password).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun sendPasswordRestEmail(email: String) = flow {
        emit(MySealed.Loading("Your Request is in Process"))
        val data = try {
            kotlinx.coroutines.delay(20000)
            authInstance.sendPasswordResetEmail(email).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun checkoutCredential(credential: PhoneAuthCredential, phoneNumber: String) = flow {
        emit(MySealed.Loading("validating OTP..."))
        val data = try {
            val auth = authInstance.signInWithCredential(credential).await()
            if (auth.user == null)
                MySealed.Success(GetConstStringObj.My_Dialog_Once)
            else {
                val doc =
                    fireStore.collection(GetConstStringObj.USERS).whereEqualTo("phone", phoneNumber)
                        .get().await()
                if (doc.isEmpty) {
                    MySealed.Success(GetConstStringObj.My_Dialog_Once)
                } else
                    MySealed.Success("Sign In Success")
            }
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)
}