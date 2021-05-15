package com.uptodd.uptoddapp.database.expectedoutcome

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.outcomes.ExpectedOutcomes

@Dao
interface ExpectedOutcomeDao {
    @Insert
    suspend fun insertAll(exout: List<ExpectedOutcomes>)

    @Query("select * from expected_outcome_table")
    fun getAll(): LiveData<List<ExpectedOutcomes>>
}