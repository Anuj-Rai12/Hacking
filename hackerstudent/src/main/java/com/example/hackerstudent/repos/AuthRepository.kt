package com.example.hackerstudent.repos

import android.util.Log
import com.example.hackerstudent.TAG
import com.example.hackerstudent.utils.*
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val actionCodeSettings: ActionCodeSettings,
    private val fireStore: FirebaseFirestore
) {
    private val authInstance by lazy {
        FirebaseAuth.getInstance()
    }
    private val currentUser by lazy {
        authInstance.currentUser
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
            Log.i(TAG, "createUserAccount: ${currentUser?.uid}")
            fireStore.collection(GetConstStringObj.USERS).document(currentUser?.uid!!)
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

    fun getUserProfileInfo() = flow {
        emit(MySealed.Loading("Profile Info Is Loading..."))
        val data = try {
            val userInfo =
                fireStore.collection(GetConstStringObj.USERS).document(currentUser?.uid!!).get()
                    .await()
            if (userInfo.exists()) {
                val user = userInfo.toObject(CreateUserAccount::class.java)
                MySealed.Success(user)
            } else
                MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


    fun updateUserName(firstname: String, lastname: String) = flow {
        emit(MySealed.Loading("Updating user name"))
        val data = try {
            val query =
                fireStore.collection(GetConstStringObj.USERS).document("${authInstance.uid}")
            query.update(
                GetConstStringObj.FIRSTNAME,
                firstname,
                GetConstStringObj.LASTNAME,
                lastname
            ).await()
            MySealed.Success("User Name Updated successfully")
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


    fun passwordRest(email: String, currentPassword: String, newPassword: String) = flow {
        emit(MySealed.Loading("Updating Users Password"))
        val data = try {
            authInstance.signInWithEmailAndPassword(email, currentPassword).await()
            authInstance.currentUser?.updatePassword(newPassword)?.await()
            fireStore.collection(GetConstStringObj.USERS)
                .document("${authInstance.uid}").update("password", newPassword).await()
            MySealed.Success("Password is Updated successfully")
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun restEmail(email: String, currentPassword: String, newEmail: String) = flow {
        emit(MySealed.Loading("Updating User Email"))
        val data = try {
            authInstance.signInWithEmailAndPassword(email, currentPassword).await()
            authInstance.currentUser?.updateEmail(newEmail)?.await()
            fireStore.collection(GetConstStringObj.USERS)
                .document("${authInstance.uid}").update(GetConstStringObj.EMAIL_ADDRESS, newEmail)
                .await()
            MySealed.Success("Email is Updated Successfully")
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


    fun addPaidCourseToUser(coursePurchase: CoursePurchase) = flow {
        emit(MySealed.Loading("Adding Course to User"))
        val data = try {
            val query =
                fireStore.collection(GetConstStringObj.USERS).document("${authInstance.uid}")
            query.update("bookmarks.${coursePurchase.course}", FieldValue.delete()).await()
            query.update("courses.${coursePurchase.course}", coursePurchase).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


    fun addCartItem(coursePurchase: CoursePurchase) = flow {
        emit(MySealed.Loading("Adding to Cart"))
        val data = try {
            val query =
                fireStore.collection(GetConstStringObj.USERS).document("${authInstance.uid}")
            query.update("bookmarks.${coursePurchase.course}", coursePurchase).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)
}