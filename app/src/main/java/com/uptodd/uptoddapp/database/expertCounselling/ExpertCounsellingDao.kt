package com.uptodd.uptoddapp.database.expertCounselling
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpertCounsellingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exp_con: ExpertCounselling)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exp_con_list: List<ExpertCounselling>)

    @Update
    suspend fun update(exp_con: ExpertCounselling)

    @Query("SELECT * FROM expert_counselling order by id asc")
    fun getAll(): LiveData<List<ExpertCounselling>>

    @Query("DELETE  FROM expert_counselling")
    suspend fun clear()
}