package com.uptodd.uptoddapp.ui.login.selectlanguage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SelectLanguageViewModel :ViewModel()
{
    val languageSelected=MutableLiveData<String>()
}