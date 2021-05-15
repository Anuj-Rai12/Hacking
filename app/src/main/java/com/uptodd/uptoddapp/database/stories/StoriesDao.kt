package com.uptodd.uptoddapp.database.stories

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.stories.Story

@Dao
interface StoriesDao {

    @Insert
    suspend fun insertAll(stories: List<Story>)

    @Query("SELECT * FROM story_table")
    fun getAll(): LiveData<List<Story>>
}