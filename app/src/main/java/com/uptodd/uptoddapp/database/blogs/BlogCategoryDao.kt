package com.uptodd.uptoddapp.database.blogs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlogCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categoryList: List<BlogCategories?>)

    @Query("select * from blogs_categories_table")
    fun getAll(): LiveData<List<BlogCategories?>>
}