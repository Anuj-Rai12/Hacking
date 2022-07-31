package com.uptodd.uptoddapp.database.freeparenting

import androidx.room.*
import com.uptodd.uptoddapp.datamodel.videocontent.Content

@Dao
interface VideoContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVideoContentItem(item: List<Content>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVideoContentItem(item: Content)

    /*@Update
    fun updateVideo(update:Content)*/

    @Query("Select * from Video_Content")
    suspend fun getVideoContentList(): List<Content>



    @Query("delete from Video_Content")
    fun deleteAllVideoContent()

}