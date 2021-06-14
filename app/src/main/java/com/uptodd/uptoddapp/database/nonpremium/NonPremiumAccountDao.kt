package com.uptodd.uptoddapp.database.nonpremium

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NonPremiumAccountDao {
    @Query("select * from non_premium_account_table")
    fun getAccountDetailsFromRoom(): LiveData<NonPremiumAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccountDetailsToRoom(account: NonPremiumAccount)
}
