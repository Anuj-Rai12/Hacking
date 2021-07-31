package com.example.hackingwork.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hackingwork.repos.AuthRepository
import com.example.hackingwork.utils.ClassPersistence
import com.example.hackingwork.utils.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrimaryViewModel @Inject constructor(
    private val classPersistence: ClassPersistence,
    private val authRepository: AuthRepository
) : ViewModel() {
    var mutableStateFlow=MutableStateFlow<UserStore?>(null)

    val read = classPersistence.read.asLiveData()

    private fun storeUserInfo(email: String, password: String, flag: Boolean) =
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
        classPersistence.storeInitUserDetail(ipAddress, firstname, lastname, phone,email,password)
    }

    fun sendEmailLinkWithToVerify(email: String) =
        authRepository.sendEmailLinkWithToVerify(email).asLiveData()
}