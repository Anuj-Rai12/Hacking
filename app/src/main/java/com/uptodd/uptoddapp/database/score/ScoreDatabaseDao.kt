package com.uptodd.uptoddapp.database.score

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScoreDatabaseDao {
    @Insert
    suspend fun insert(score: Score)

    @Update
    suspend fun update(score: Score)

    @Query("DELETE FROM score_table")
    suspend fun clear()

    @Query("UPDATE score_table SET completed_todos = completed_todos + 1 WHERE id = :id")
    suspend fun incrementTodoScoreOfType(id: Int)

    @Query("UPDATE score_table SET total_todos = total_todos + 1 WHERE id = :id")
    suspend fun incrementTotalTodosOfType(id: Int)

    @Query("SELECT completed_todos FROM score_table WHERE id = :id")
    fun getNumberOfCompletedTodosOfType(id: Int): LiveData<Int>

    @Query("SELECT total_todos FROM score_table WHERE id = :id")
    fun getTotalNumberOfTodosOfType(id: Int): LiveData<Int>

    @Query("SELECT * FROM score_table WHERE id = :id")
    suspend fun getScoreOfType(id: Int): Score

}