package com.uptodd.uptoddapp.database.activitypodcast

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActivityPodcastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(act_pod: ActivityPodcast)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(act_pod_list: List<ActivityPodcast>)

    @Update
    suspend fun update(act_pod: ActivityPodcast)

    @Query("SELECT * FROM activity_podcast_table")
    fun getAll(): LiveData<List<ActivityPodcast>>
}