package com.uptodd.uptoddapp.database.blogs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blogList: List<Blogs?>)

    @Query("select * from blogs_table")
    fun getAll(): LiveData<List<Blogs?>>

    @Query("select * from blogs_table where blogCategory = :categoryId")
    fun getAllById(categoryId: Long): LiveData<List<Blogs?>>
}