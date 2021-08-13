package com.example.hackingwork.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hackingwork.utils.Module
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(): ViewModel() {
 var moduleMap:MutableMap<String,Module>?=null
}