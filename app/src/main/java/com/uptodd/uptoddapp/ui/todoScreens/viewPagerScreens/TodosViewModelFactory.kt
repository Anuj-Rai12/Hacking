package com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uptodd.uptoddapp.database.UptoddDatabase

class TodosViewModelFactory(
    private val database : UptoddDatabase,
    private val period: Int,
    private val application: Application,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodosViewModel::class.java)) {
            return TodosViewModel( database, period, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}