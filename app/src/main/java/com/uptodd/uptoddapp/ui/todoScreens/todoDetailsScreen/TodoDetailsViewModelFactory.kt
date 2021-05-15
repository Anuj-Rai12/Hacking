package com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TodoDetailsViewModelFactory(
    private val application: Application? = null,
    private val todoId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(EditAlarmViewModel::class.java)) {
            return EditAlarmViewModel(application!!, todoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}