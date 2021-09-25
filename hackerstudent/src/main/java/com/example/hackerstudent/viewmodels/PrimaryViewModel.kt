package com.example.hackerstudent.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hackerstudent.utils.ClassPersistence
import com.example.hackerstudent.repos.AuthRepository
import com.example.hackerstudent.utils.CoursePurchase
import com.example.hackerstudent.utils.LocalCoursePurchase
import com.example.hackerstudent.utils.UserStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrimaryViewModel @Inject constructor(
    private val classPersistence: ClassPersistence,
    private val authRepository: AuthRepository
) : ViewModel() {
    //For CreateUserAccount.kt
    var mutableStateFlow = MutableStateFlow<UserStore?>(null)
    var paymentLayout = MutableStateFlow<LocalCoursePurchase?>(null)

    // For PhoneOtp
    var credential: PhoneAuthCredential? = null
    val read = classPersistence.read.asLiveData()
    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

    fun storeUserInfo(email: String, password: String, flag: Boolean) =
        viewModelScope.launch {
            classPersistence.updateInfo(email, password, flag)
        }

    fun storeInitUserDetail(
        ipAddress: String,
        firstname: String,
        lastname: String,
        phone: String,
        password: String,
        email: String
    ) = viewModelScope.launch {
        classPersistence.storeInitUserDetail(ipAddress, firstname, lastname, phone, email, password)
    }

    fun updatePassword(password: String, TAG: String) {
        viewModelScope.launch {
            classPersistence.updatePassword(password, TAG)
        }
    }

    val userInfo = authRepository.getUserProfileInfo().asLiveData()

    fun sendEmailLinkWithToVerify(email: String) =
        authRepository.sendEmailLinkWithToVerify(email).asLiveData()

    fun createInWithEmail(email: String, link: String) =
        authRepository.createInWithEmail(email, link).asLiveData()

    fun updatePhoneNumber(credential: PhoneAuthCredential, password: String) =
        authRepository.updatePhoneNumber(credential, password).asLiveData()

    fun createUserAccount(userStore: UserStore) =
        authRepository.createUserAccount(userStore).asLiveData()

    fun checkEmailOfUsers(email: String, password: String) =
        authRepository.checkEmailOfUsers(email, password).asLiveData()

    fun sendPasswordRestEmail(email: String) =
        authRepository.sendPasswordRestEmail(email).asLiveData()

    fun checkoutCredential(credential: PhoneAuthCredential, phone: String) =
        authRepository.checkoutCredential(credential, phone).asLiveData()

    fun updateUserName(firstname: String, lastname: String) =
        authRepository.updateUserName(firstname, lastname).asLiveData()

    fun updatePassword(email: String, currentPassword: String, newPassword: String) =
        authRepository.passwordRest(email, currentPassword, newPassword).asLiveData()

    fun updateEmail(email: String, currentPassword: String, newEmail: String) =
        authRepository.restEmail(email, currentPassword, newEmail).asLiveData()

    fun addPaidCourseToUser(coursePurchase: CoursePurchase) =
        authRepository.addPaidCourseToUser(coursePurchase).asLiveData()

    fun addItemCart(coursePurchase: CoursePurchase) =
        authRepository.addCartItem(coursePurchase).asLiveData()
}