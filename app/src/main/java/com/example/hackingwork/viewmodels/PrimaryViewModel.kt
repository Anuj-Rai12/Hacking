package com.example.hackingwork.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.hackingwork.utils.ClassPersistence
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel
class PrimaryViewModel(
    private val classPersistence: ClassPersistence
) : ViewModel() {

    val read = classPersistence.read.asLiveData()

    private fun storeUserInfo(email: String, password: String, flag: Boolean) =
        viewModelScope.launch {
            classPersistence.updateInfo(email, password, flag)
        }

    fun storeInitUserDetail(
        ipAddress: String,
        firstname: String,
        lastname: String,
        phone: String
    ) = viewModelScope.launch {
        classPersistence.storeInitUserDetail(ipAddress, firstname, lastname, phone)
    }

}