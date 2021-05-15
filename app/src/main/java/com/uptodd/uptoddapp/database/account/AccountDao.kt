package com.uptodd.uptoddapp.database.account

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AccountDao {
    @Query("select * from account_table")
    fun getAccountDetailsFromRoom(): LiveData<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccountDetailsToRoom(account: Account)
}

@Database(entities = [Account::class], version = 4, exportSchema = false)
abstract class AccountDatabase : RoomDatabase() {
    abstract val accountDatabaseDao: AccountDao
}

private lateinit var INSTANCE: AccountDatabase

fun getDatabase(context: Context): AccountDatabase {
    if (!::INSTANCE.isInitialized) {
        INSTANCE =
            Room.databaseBuilder(
                context.applicationContext,
                AccountDatabase::class.java,
                "account_database"
            )
                .fallbackToDestructiveMigration().build()

    }
    return INSTANCE
}