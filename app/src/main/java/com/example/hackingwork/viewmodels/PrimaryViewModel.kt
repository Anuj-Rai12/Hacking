package com.example.hackingwork.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hackingwork.repos.AuthRepository
import com.example.hackingwork.utils.ClassPersistence
import com.example.hackingwork.utils.UserStore
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

    val update = authRepository.getVersionControl().asLiveData()

    // For PhoneOtp
    var credential: PhoneAuthCredential? = null
    val read = classPersistence.read.asLiveData()

    fun storeUserInfo(email: String, password: String, flag: Boolean) =
        viewModelScope.launch {
            classPersistence.updateInfo(email, password, flag)
        }

    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
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
}