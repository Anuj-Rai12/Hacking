package com.uptodd.uptoddapp.database.media.music


import androidx.room.*


@Dao
interface MusicFilesDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(musicFile: MusicFiles)

    @Update
    suspend fun update(musicFile: MusicFiles)

    @Delete
    suspend fun delete(musicFile: MusicFiles)

    @Query("SELECT * from music_files WHERE id = :key")
    suspend fun get(key: Long): MusicFiles?

    @Query("DELETE FROM music_files")
    suspend fun clear()

    @Query("SELECT * FROM music_files WHERE language = 'NA' AND prenatal=-1")
    suspend fun getAllDownloadedMusic(): List<MusicFiles>

    @Query("SELECT * FROM music_files WHERE language != 'NA'")
    suspend fun getAllDownloadedPoem(): List<MusicFiles>

    @Query("SELECT * FROM music_files WHERE prenatal!=-1")
    suspend fun getAllSpeedBoosterFiles(): List<MusicFiles>

    @Query("SELECT * FROM music_files")
    suspend fun getAllFiles(): List<MusicFiles>

    @Query("SELECT filePath FROM music_files WHERE id = :id")
    suspend fun getFilePath(id: Int): String

}
