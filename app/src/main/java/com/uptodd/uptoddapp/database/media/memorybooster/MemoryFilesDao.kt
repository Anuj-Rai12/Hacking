package com.uptodd.uptoddapp.database.media.memorybooster

import androidx.room.*
import com.uptodd.uptoddapp.database.media.music.MusicFiles

@Dao
interface MemoryFilesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(musicFile: MemoryBoosterFiles)

    @Update
    suspend fun update(musicFile: MemoryBoosterFiles)

    @Delete
    suspend fun delete(musicFile: MemoryBoosterFiles)

    @Query("SELECT * from memory_files WHERE id = :key")
    suspend fun get(key: Long): MemoryBoosterFiles?

    @Query("DELETE FROM memory_files")
    suspend fun clear()

    @Query("SELECT * FROM memory_files")
    suspend fun getAllFiles(): List<MemoryBoosterFiles>

    @Query("SELECT filePath FROM memory_files WHERE id = :id")
    suspend fun getFilePath(id: Int): String

}
