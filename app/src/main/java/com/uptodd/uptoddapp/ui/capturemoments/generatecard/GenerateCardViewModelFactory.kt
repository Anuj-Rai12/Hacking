package com.uptodd.uptoddapp.ui.capturemoments.generatecard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenerateCardViewModelFactory(val app:Application,val type:String?,val token:String?) : ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GenerateCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GenerateCardViewModel(app,type,token) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}