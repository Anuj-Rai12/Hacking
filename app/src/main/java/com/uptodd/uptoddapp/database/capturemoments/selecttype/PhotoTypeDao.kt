package com.uptodd.uptoddapp.database.capturemoments.selecttype

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoTypeDao
{
    @Query("select * from photo_type_table")
    fun getPhotoTypesFromRoom(): LiveData<List<PhotoType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotoTypesToRoom(photoTypes:List<PhotoType>)
}

@Database(entities = [PhotoType::class],version=1,exportSchema = false)
abstract class PhotoTypeDatabase : RoomDatabase() {
    abstract val photoTypeDatabaseDao: PhotoTypeDao
}

private lateinit var INSTANCE:PhotoTypeDatabase

fun getDatabase(context: Context):PhotoTypeDatabase
{
    if(!::INSTANCE.isInitialized)
    {
        INSTANCE=
            Room.databaseBuilder(context.applicationContext,PhotoTypeDatabase::class.java,"photo_type_database")
                .fallbackToDestructiveMigration().build()

    }
    return INSTANCE
}
