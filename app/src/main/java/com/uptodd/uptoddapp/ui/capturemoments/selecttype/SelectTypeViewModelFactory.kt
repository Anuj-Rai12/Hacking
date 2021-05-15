package com.uptodd.uptoddapp.ui.capturemoments.selecttype

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SelectTypeViewModelFactory(val app: Application,val token:String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectTypeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectTypeViewModel(app,token) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}