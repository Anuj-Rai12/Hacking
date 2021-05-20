package com.uptodd.uptoddapp.database.media.resource

import androidx.room.*



@Dao
interface ResourceFilesDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resourceFiles: ResourceFiles)

    @Update
    suspend fun update(resourceFiles: ResourceFiles)

    @Delete
    suspend fun delete(resourceFiles: ResourceFiles)

    @Query("SELECT * from resource_files WHERE id = :key")
    suspend fun get(key: Long): ResourceFiles?

    @Query("DELETE FROM resource_files")
    suspend fun clear()

    @Query("SELECT * FROM resource_files")
    suspend fun getAllFiles(): List<ResourceFiles>

    @Query("SELECT filePath FROM resource_files WHERE id = :id")
    suspend fun getFilePath(id: Int): String

}
