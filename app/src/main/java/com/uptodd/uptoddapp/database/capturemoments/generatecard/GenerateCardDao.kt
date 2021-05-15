package com.uptodd.uptoddapp.database.capturemoments.generatecard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GenerateCardDao
{
    @Query("select * from card_table where category= :key")
    fun getCardsFromRoom(key:String): LiveData<List<Card>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCardsToRoom(cards:List<Card>)
}

@Database(entities = [Card::class],version=2,exportSchema = false)
abstract class GenerateCardDatabase : RoomDatabase() {
    abstract val generateCardDatabaseDao: GenerateCardDao
}

private lateinit var INSTANCE:GenerateCardDatabase

fun getDatabase(context: Context):GenerateCardDatabase
{
    if(!::INSTANCE.isInitialized)
    {
        INSTANCE=
            Room.databaseBuilder(context.applicationContext,GenerateCardDatabase::class.java,"generate_card_database")
                .fallbackToDestructiveMigration().build()

    }
    return INSTANCE
}
