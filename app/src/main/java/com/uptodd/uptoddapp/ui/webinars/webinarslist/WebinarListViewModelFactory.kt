package com.uptodd.uptoddapp.ui.webinars.webinarslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uptodd.uptoddapp.database.UptoddDatabase

class WebinarListViewModelFactory(private val webinarsDatabase: UptoddDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(WebinarsListViewModel::class.java)) {
            return WebinarsListViewModel(webinarsDatabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel.")
    }
}