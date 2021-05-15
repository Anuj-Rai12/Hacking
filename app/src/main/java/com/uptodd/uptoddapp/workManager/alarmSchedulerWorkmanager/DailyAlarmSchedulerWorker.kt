package com.uptodd.uptoddapp.workManager.alarmSchedulerWorkmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.database.todo.TodoDatabaseDao
import com.uptodd.uptoddapp.helperClasses.DateClass
import kotlinx.coroutines.*

class DailyAlarmSchedulerWorker(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    lateinit var todoDatabase: TodoDatabaseDao

    // coroutine job
    val job = Job()
    val uiScope = CoroutineScope(Dispatchers.Main + job)


    // this worker auto schedules daily alarms, whether or not the user
    // has switched on his internet
    override suspend fun doWork(): Result = coroutineScope {
        todoDatabase = UptoddDatabase.getInstance(context).todoDatabaseDao

        val dailyTodosForAutosetAlarm =
            todoDatabase.getDailyTodosForAlarmAutoset(period = getPeriod(context))
        for (todo in dailyTodosForAutosetAlarm) {
            val alarmTimeInMilli =
                DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)


            if (todo.lastSwipeDate == DateClass().getCurrentDateAsString()) {
                // cancel rescheduling of alarm if to-do was swiped on the same day
                Log.d("workmanager", "cancelling re-schedule: todo was swiped today only")
                continue
            } else {
                if (alarmTimeInMilli != null) {
                    UptoddAlarm.setAlarm(
                        context,
                        alarmTimeInMilli,
                        todo.id,
                        todo.task
                    )
                    updateAlarmWasScheduledToDatabase(todo)
                    Log.d("auto", "successful schedule alarm for $todo")
                } else {
                    Log.d("auto", "couldnt schedule alarm for $todo")
                }
            }
        }

        Log.d("workmanager", "Successfully fired for daily alarms scheduler")
        Result.success()
    }

    private suspend fun updateAlarmWasScheduledToDatabase(todo: Todo) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                todo.isAlarmSet = true
                todo.isAlarmNeededByUser = true
                todoDatabase.update(todo)
            }
        }
    }

}