package com.uptodd.uptoddapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.media.music.MusicViewModel
import com.uptodd.uptoddapp.media.poem.PoemViewModel
import com.uptodd.uptoddapp.media.memorybooster.MemoryBoosterViewModel
import com.uptodd.uptoddapp.ui.account.account.AccountViewModel
import com.uptodd.uptoddapp.ui.blogs.blogslist.BlogsListViewModel
import com.uptodd.uptoddapp.ui.capturemoments.selecttype.SelectTypeViewModel
import com.uptodd.uptoddapp.ui.todoScreens.ranking.RankingViewModel
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.TodosViewModel
import com.uptodd.uptoddapp.ui.webinars.webinarslist.WebinarsListViewModel

@Suppress("UNCHECKED_CAST")
class UptoddViewModelFactory private constructor(
    private val application: Application
) : ViewModelProvider.Factory {

    companion object {
        private lateinit var uptoddViewModelFactory: UptoddViewModelFactory

        fun getInstance(
            application: Application
        ): UptoddViewModelFactory {
            if (!this::uptoddViewModelFactory.isInitialized) {
                uptoddViewModelFactory = UptoddViewModelFactory(application)
            }

            return uptoddViewModelFactory
        }
    }

    private val preferences = application.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
    private val database = UptoddDatabase.getInstance(application)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        // music viewmodel
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(database.musicDatabaseDao, application) as T
        }

        // poem viewmodel

        if (modelClass.isAssignableFrom(PoemViewModel::class.java)) {
            return PoemViewModel(database.musicDatabaseDao, application) as T
        }

        // account viewModel

        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            val uid = preferences.getString("uid", "")
            val token = preferences.getString("token", "")
            return AccountViewModel(application, uid, token) as T
        }

        // blog viewmodel

        if (modelClass.isAssignableFrom(BlogsListViewModel::class.java)) {
            return BlogsListViewModel(database) as T
        }


        // webinar viewmodel

        if (modelClass.isAssignableFrom(WebinarsListViewModel::class.java)) {
            return WebinarsListViewModel(database) as T
        }

        // ranking viewmodel
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            val uid = preferences.getString("uid", "")
            return RankingViewModel(uid) as T
        }

        // todos viewmodel

        if (modelClass.isAssignableFrom(TodosViewModel::class.java)) {
            val period = getPeriod(application)
            return TodosViewModel(database, period, application) as T
        }

        // select type viewmodel
        if (modelClass.isAssignableFrom(SelectTypeViewModel::class.java)) {
            val token = preferences.getString("token", "")
            return SelectTypeViewModel(application, token) as T
        }

        // speedbooster viewmodel
        if(modelClass.isAssignableFrom(MemoryBoosterViewModel::class.java))
        {
            return   return MemoryBoosterViewModel(database.musicDatabaseDao, application) as T
        }


        throw IllegalArgumentException("Unknown ViewModel")
    }
}