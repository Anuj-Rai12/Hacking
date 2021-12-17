package com.uptodd.uptoddapp.database.activitysample

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActivitySampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(act_sam: ActivitySample)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(act_sam_list: List<ActivitySample>)

    @Update
    suspend fun update(act_sam: ActivitySample)

    @Query("SELECT * FROM activity_sample_table order by id asc")
    fun getAll(): LiveData<List<ActivitySample>>

}