package com.uptodd.uptoddapp.alarmsAndNotifications.ActivityAlarm

import android.content.Context
import android.util.Log
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.helperClasses.DateClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyActivityAlarmScheduler private constructor(private val context: Context) {

    companion object {
        private lateinit var dailyActivityAlarmScheduler: DailyActivityAlarmScheduler

        fun setupAlarm(context: Context) {
            if (!this::dailyActivityAlarmScheduler.isInitialized) {
                dailyActivityAlarmScheduler = DailyActivityAlarmScheduler(context)
            }

            dailyActivityAlarmScheduler.scheduleAlarms()

        }
    }

    private var todoDatabase = UptoddDatabase.getInstance(context).todoDatabaseDao
    val ioScope = CoroutineScope(Dispatchers.IO)


    fun scheduleAlarms() = ioScope.launch {
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
//                    UptoddAlarm.setAlarm(
//                        context,
//                        alarmTimeInMilli,
//                        todo.id,
//                        todo.task
//                    )
                    UptoddAlarm.setRepeatingAlarm(
                        context,
                        alarmTimeInMilli,
                        DAILY_INTERVAL,
                        todo.id,
                        todo.task
                    )

                    todo.isAlarmSet = true
                    todo.isAlarmNeededByUser = true
                    todoDatabase.update(todo)

                    Log.d("auto", "successful schedule alarm for $todo")
                } else {
                    Log.d("auto", "couldnt schedule alarm for $todo")
                }
            }
        }

    }
}