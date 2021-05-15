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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class WeeklyAlarmSchedulerWorker(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    lateinit var todoDatabase: TodoDatabaseDao

    // coroutine job

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    override suspend fun doWork(): Result {
        todoDatabase = UptoddDatabase.getInstance(context).todoDatabaseDao
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.DAY_OF_WEEK)) {     // todays day
            Calendar.MONDAY -> coroutineScope.launch {
                // get monday todos and setup the alarms
                val todos = todoDatabase.getWeeklyMondayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.TUESDAY -> coroutineScope.launch {
                val todos = todoDatabase.getWeeklyTuesdayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.WEDNESDAY -> coroutineScope.launch {
                val todos = todoDatabase.getWeeklyWednesdayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.THURSDAY -> coroutineScope.launch {
                val todos = todoDatabase.getWeeklyThursdayAlarms(period = getPeriod(context))
            }
            Calendar.FRIDAY -> coroutineScope.launch {
                // get monday todos and setup the alarms
                val todos = todoDatabase.getWeeklyFridayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.SATURDAY -> coroutineScope.launch {
                // get monday todos and setup the alarms
                val todos = todoDatabase.getWeeklySaturdayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.SUNDAY -> coroutineScope.launch {
                // get monday todos and setup the alarms
                val todos = todoDatabase.getWeeklySundayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
        }


        return Result.success()
    }


    private fun scheduleAlarms(todos: List<Todo>) {
        for (todo in todos) {
            val alarmTimeInMilli =
                DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
            if (alarmTimeInMilli != null) {
                UptoddAlarm.setAlarm(
                    context,
                    alarmTimeInMilli,
                    todo.id,
                    todo.task
                )
                todo.isAlarmSet = true
                todo.isAlarmNeededByUser = true
                coroutineScope.launch {
                    todoDatabase.update(todo)
                }
                Log.d("weeklyworker", "alarm scheduled for $todo")

            } else {
                Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
            }
        }
    }
}


//private fun scheduleMondayAlarms() {
//    uiScope.launch {
//        withContext(Dispatchers.IO) {
//            val mondayTodos = todoDatabase.getWeeklyMondayAlarms(period = getPeriod(context))
//            for (todo in mondayTodos) {
//                val alarmTimeInMilli =
//                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
//                if (alarmTimeInMilli != null) {
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
//                    todo.isAlarmSet = true
//                    todo.isAlarmNeededByUser = true
//                    todoDatabase.update(todo)
//                    Log.d("weeklyworker", "alarm scheduled for $todo")
//
//                } else {
//                    Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
//                }
//            }
//        }
//    }
//}
//
//private fun scheduleTuesdayAlarms() {
//    uiScope.launch {
//        withContext(Dispatchers.IO) {
//            val todos = todoDatabase.getWeeklyTuesdayAlarms(period = getPeriod(context))
//            for (todo in todos) {
//                val alarmTimeInMilli =
//                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
//                if (alarmTimeInMilli != null) {
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
//                    todo.isAlarmSet = true
//                    todo.isAlarmNeededByUser = true
//                    todoDatabase.update(todo)
//                    Log.d("weeklyworker", "alarm scheduled for $todo")
//
//                } else {
//                    Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
//                }
//            }
//        }
//    }
//}
//
//private fun scheduleWednesdayAlarms() {
//    uiScope.launch {
//        withContext(Dispatchers.IO) {
//            val todos = todoDatabase.getWeeklyWednesdayAlarms(period = getPeriod(context))
//            for (todo in todos) {
//                val alarmTimeInMilli =
//                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
//                if (alarmTimeInMilli != null) {
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
//                    todo.isAlarmSet = true
//                    todo.isAlarmNeededByUser = true
//                    todoDatabase.update(todo)
//                    Log.d("weeklyworker", "alarm scheduled for $todo")
//
//                } else {
//                    Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
//                }
//            }
//        }
//    }
//}
//
//private fun scheduleThursdayAlarms() {
//    uiScope.launch {
//        withContext(Dispatchers.IO) {
//            val todos = todoDatabase.getWeeklyThursdayAlarms(period = getPeriod(context))
//            for (todo in todos) {
//                val alarmTimeInMilli =
//                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
//                if (alarmTimeInMilli != null) {
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
//                    todo.isAlarmSet = true
//                    todo.isAlarmNeededByUser = true
//                    todoDatabase.update(todo)
//                    Log.d("weeklyworker", "alarm scheduled for $todo")
//
//                } else {
//                    Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
//                }
//            }
//        }
//    }
//}
//
//private fun scheduleFridayAlarms() {
//    uiScope.launch {
//        withContext(Dispatchers.IO) {
//            val todos = todoDatabase.getWeeklyFridayAlarms(period = getPeriod(context))
//            for (todo in todos) {
//                val alarmTimeInMilli =
//                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
//                if (alarmTimeInMilli != null) {
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
//                    todo.isAlarmSet = true
//                    todo.isAlarmNeededByUser = true
//                    todoDatabase.update(todo)
//                    Log.d("weeklyworker", "alarm scheduled for $todo")
//
//                } else {
//                    Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
//                }
//            }
//        }
//    }
//}
//
//private fun scheduleSaturdayAlarms() {
//    uiScope.launch {
//        withContext(Dispatchers.IO) {
//            val todos = todoDatabase.getWeeklySaturdayAlarms(period = getPeriod(context))
//            for (todo in todos) {
//                val alarmTimeInMilli =
//                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
//                if (alarmTimeInMilli != null) {
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
//                    todo.isAlarmSet = true
//                    todo.isAlarmNeededByUser = true
//                    todoDatabase.update(todo)
//                    Log.d("weeklyworker", "alarm scheduled for $todo")
//
//                } else {
//                    Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
//                }
//            }
//        }
//    }
//}
//
//private fun scheduleSundayAlarms() {
//    uiScope.launch {
//        withContext(Dispatchers.IO) {
//            val todos = todoDatabase.getWeeklySundayAlarms(period = getPeriod(context))
//            for (todo in todos) {
//                val alarmTimeInMilli =
//                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
//                if (alarmTimeInMilli != null) {
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
//                    todo.isAlarmSet = true
//                    todo.isAlarmNeededByUser = true
//                    todoDatabase.update(todo)
//                    Log.d("weeklyworker", "alarm scheduled for $todo")
//
//                } else {
//                    Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
//                }
//            }
//        }
//    }
//}


