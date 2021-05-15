package com.uptodd.uptoddapp.alarmsAndNotifications.ActivityAlarm

import android.content.Context
import android.util.Log
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.helperClasses.DateClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class WeeklyActivtyAlarmScheduler private constructor(private val context: Context) {

    companion object {
        private lateinit var weeklyActivtyAlarmScheduler: WeeklyActivtyAlarmScheduler

        fun setupAlarm(context: Context) {
            if (!this::weeklyActivtyAlarmScheduler.isInitialized) {
                weeklyActivtyAlarmScheduler = WeeklyActivtyAlarmScheduler(context)
            }

            weeklyActivtyAlarmScheduler.scheduleWeeklyAlarms()

        }
    }

    private var todoDatabase = UptoddDatabase.getInstance(context).todoDatabaseDao

    private val ioScope = CoroutineScope(Dispatchers.IO)


    fun scheduleWeeklyAlarms() {
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> ioScope.launch {
                // get monday todos and setup the alarms
                val todos = todoDatabase.getWeeklyMondayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.TUESDAY -> ioScope.launch {
                val todos = todoDatabase.getWeeklyTuesdayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.WEDNESDAY -> ioScope.launch {
                val todos = todoDatabase.getWeeklyWednesdayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.THURSDAY -> ioScope.launch {
                val todos = todoDatabase.getWeeklyThursdayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.FRIDAY -> ioScope.launch {
                // get monday todos and setup the alarms
                val todos = todoDatabase.getWeeklyFridayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.SATURDAY -> ioScope.launch {
                // get monday todos and setup the alarms
                val todos = todoDatabase.getWeeklySaturdayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
            Calendar.SUNDAY -> ioScope.launch {
                // get sunday todos and setup the alarms
                val todos = todoDatabase.getWeeklySundayAlarms(period = getPeriod(context))
                scheduleAlarms(todos)
            }
        }
    }

    private fun scheduleAlarms(todos: List<Todo>) {
        for (todo in todos) {
            val alarmTimeInMilli =
                DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
            if (alarmTimeInMilli != null) {
//                UptoddAlarm.setAlarm(
//                    context,
//                    alarmTimeInMilli,
//                    todo.id,
//                    todo.task
//                )
                UptoddAlarm.setRepeatingAlarm(
                    context,
                    alarmTimeInMilli,
                    WEEKLY_INTERVAL,
                    todo.id,
                    todo.task
                )

                todo.isAlarmSet = true
                todo.isAlarmNeededByUser = true
                ioScope.launch {
                    todoDatabase.update(todo)
                }
                Log.d("weeklyworker", "alarm scheduled for $todo")

            } else {
                Log.d("weeklyAlarmWorker", "alarmtimeinmilli is null")
            }
        }
    }
}