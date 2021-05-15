package com.uptodd.uptoddapp.database.todo

import androidx.lifecycle.LiveData
import androidx.room.*
import com.uptodd.uptoddapp.database.score.DAILY_TODO
import com.uptodd.uptoddapp.database.score.ESSENTIALS_TODO
import com.uptodd.uptoddapp.database.score.MONTHLY_TODO


@Dao
interface TodoDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: Todo)

    @Update
    suspend fun update(todo: Todo)

    @Query("DELETE FROM todo_table")
    suspend fun clear()

    @Query("SELECT * FROM todo_table WHERE id = :todoId")
    suspend fun getTodo(todoId: Int): Todo?

    @Query("SELECT is_alarm_set FROM todo_table WHERE id = :todoId")
    fun getAlarmScheduledStatus(todoId: Int): LiveData<Boolean>

    @Query("SELECT * FROM todo_table WHERE type = :todoType AND is_completed = :completionStatus AND period = :period ORDER BY alarm_time_by_user_in_milli DESC ")
    fun getAllPendingTodosOfType(
        todoType: Int,
        completionStatus: Boolean = false,
        period: Int
    ): LiveData<List<Todo>>

    @Query("SELECT COUNT(*) FROM todo_table WHERE type = :todoType AND is_completed = :completionStatus AND period = :period ORDER BY alarm_time_by_user_in_milli DESC")
    fun getAllPendingTodosCountOfType(
        todoType: Int,
        completionStatus: Boolean = false,
        period: Int
    ): Int

    @Query("SELECT COUNT(*) FROM todo_table WHERE id = :todoId")
    suspend fun checkTodo(todoId: Int): Int

    @Query("SELECT * FROM todo_table")
    suspend fun fetchAllTodosFromDB(): List<Todo>

    @Query("SELECT * FROM todo_table WHERE type = :todoType ORDER BY id DESC LIMIT 1 ")
    suspend fun getLatestTodoOfType(todoType: Int): Todo

    @Query("SELECT * FROM todo_table WHERE is_alarm_needed_by_user = :isAlarmNeededByUser AND type = :todoType AND is_alarm_set = :isAlarmSet and period = :period ")
    suspend fun getDailyTodosForAlarmAutoset(
        isAlarmNeededByUser: Boolean = true,
        todoType: Int = DAILY_TODO,
        isAlarmSet: Boolean = false, period: Int
    ): List<Todo>

    @Query("SELECT * FROM todo_table WHERE weekly_monday = :status and period = :period")
    suspend fun getWeeklyMondayAlarms(status: Boolean = true, period: Int): List<Todo>

    @Query("SELECT * FROM todo_table WHERE weekly_tuesday = :status and period = :period")
    suspend fun getWeeklyTuesdayAlarms(status: Boolean = true, period: Int): List<Todo>

    @Query("SELECT * FROM todo_table WHERE weekly_wednesday = :status and period = :period")
    suspend fun getWeeklyWednesdayAlarms(status: Boolean = true, period: Int): List<Todo>

    @Query("SELECT * FROM todo_table WHERE weekly_thursday = :status and period = :period")
    suspend fun getWeeklyThursdayAlarms(status: Boolean = true, period: Int): List<Todo>

    @Query("SELECT * FROM todo_table WHERE weekly_friday = :status and period = :period")
    suspend fun getWeeklyFridayAlarms(status: Boolean = true, period: Int): List<Todo>

    @Query("SELECT * FROM todo_table WHERE weekly_saturday = :status and period = :period")
    suspend fun getWeeklySaturdayAlarms(status: Boolean = true, period: Int): List<Todo>

    @Query("SELECT * FROM todo_table WHERE weekly_sunday = :status and period = :period")
    suspend fun getWeeklySundayAlarms(status: Boolean = true, period: Int): List<Todo>

    @Query("SELECT COUNT (*) FROM todo_table WHERE type = :todoType and is_completed = :completionStatus and period = :period")
    suspend fun getCountOfTodosCompletedOfType(
        todoType: Int,
        completionStatus: Boolean = true,
        period: Int
    ): Int

    @Query("SELECT COUNT (*) FROM todo_table WHERE type = :todoType and period = :period")
    suspend fun getCountOfTodosOfType(todoType: Int, period: Int): Int

    @Query("SELECT * FROM todo_table WHERE is_alarm_needed_by_user = :isAlarmNeededByUser and is_alarm_set = :isAlarmSet and type = :todoType and is_completed = :completionStatus and period = :period")
    suspend fun getMonthlyTodosForAlarm(
        isAlarmNeededByUser: Boolean = true,
        isAlarmSet: Boolean = false,
        todoType: Int = MONTHLY_TODO,
        completionStatus: Boolean = false, period: Int
    ): List<Todo>

    @Query("SELECT * FROM todo_table WHERE is_alarm_needed_by_user = :isAlarmNeededByUser and is_alarm_set = :isAlarmSet and type = :todoType and is_completed = :completionStatus and period = :period")
    suspend fun getEssentialsTodosForAlarm(
        isAlarmNeededByUser: Boolean = true,
        isAlarmSet: Boolean = false,
        todoType: Int = ESSENTIALS_TODO,
        completionStatus: Boolean = false, period: Int
    ): List<Todo>

    @Query("SELECT * FROM todo_table WHERE type = :todoType AND period = :period")
    suspend fun getTodosOfType(todoType: Int, period: Int): List<Todo>


}