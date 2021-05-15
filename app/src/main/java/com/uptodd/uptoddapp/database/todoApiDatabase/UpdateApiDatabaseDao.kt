package com.uptodd.uptoddapp.database.todoApiDatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UpdateApiDatabaseDao {
    @Insert()
    suspend fun createRow(updateApi: UpdateApi)

    @Update
    suspend fun update(updateApi: UpdateApi)

    @Query("SELECT COUNT(*) FROM update_api_table WHERE date = :date AND type = :type")
    suspend fun checkRowAvailable(date: String, type: Int): Int

    @Query("SELECT * FROM update_api_table WHERE date = :date AND type = :type")
    suspend fun getRow(date: String, type: Int): UpdateApi

    @Query("SELECT * FROM update_api_table WHERE workManagerId = :workManagerId")
    suspend fun getUpdateApiFromId(workManagerId: Int): UpdateApi

    @Query("SELECT workManagerId FROM update_api_table ORDER BY workManagerId DESC LIMIT 1")
    suspend fun getIdOfLastUpdateApi(): Int

    @Query("SELECT * FROM update_api_table WHERE is_updated = :updateStatus and type = :todoType")
    suspend fun pendingTodosToUpload(todoType: Int, updateStatus: Boolean = false): List<UpdateApi>

    @Query("SELECT * FROM update_api_table")
    suspend fun getAllData(): List<UpdateApi>

}