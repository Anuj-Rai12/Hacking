package com.uptodd.uptoddapp.alarmsAndNotifications.ActivityAlarm

import android.content.Context
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.todo.TWENTY_FOUR_HOURS_IN_MILLI
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.database.todo.TodoDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MonthlyAndEssentialActivityAlarmScheduler private constructor(
    private val context: Context
) {

    companion object {
        private lateinit var monthlyAndEssentialActivityAlarmScheduler: MonthlyAndEssentialActivityAlarmScheduler

        fun setupAlarm(context: Context) {
            if (!this::monthlyAndEssentialActivityAlarmScheduler.isInitialized) {
                monthlyAndEssentialActivityAlarmScheduler =
                    MonthlyAndEssentialActivityAlarmScheduler(context)
            }

            monthlyAndEssentialActivityAlarmScheduler.scheduleMonthlyAndEssentialAlarm()

        }
    }

    private var todoDatabase: TodoDatabaseDao = UptoddDatabase.getInstance(context).todoDatabaseDao

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private fun scheduleMonthlyAndEssentialAlarm() = ioScope.launch {
        val monthlyPendingTodos: List<Todo> =
            todoDatabase.getMonthlyTodosForAlarm(period = getPeriod(context))
        val essentialsPendingTodos: List<Todo> =
            todoDatabase.getEssentialsTodosForAlarm(period = getPeriod(context))

        val arrayList = ArrayList<Todo>()
        arrayList.addAll(monthlyPendingTodos)
        arrayList.addAll(essentialsPendingTodos)

        for (todo in arrayList) {
            scheduleForFirstMonday(todo)
            scheduleForSecondMonday(todo)
            scheduleForLastSaturday(todo)
            scheduleForLastSunday(todo)
        }
    }

    private fun scheduleForFirstMonday(todo: Todo) {
        val hour = todo.alarmTimeByUser.substringBefore(":").toInt()
        val minute = todo.alarmTimeByUser.substringBeforeLast(":").substringAfter(":").toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)


        val timeInMilli = calendar.time.time    // second time converts time to timeInMilli
        if (timeInMilli - System.currentTimeMillis() in 1..TWENTY_FOUR_HOURS_IN_MILLI) {
            scheduleAlarm(timeInMilli, todo)
        }
    }

    private fun scheduleForSecondMonday(todo: Todo) {
        val hour = todo.alarmTimeByUser.substringBefore(":").toInt()
        val minute = todo.alarmTimeByUser.substringBeforeLast(":").substringAfter(":").toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)


        val timeInMilli = calendar.time.time    // second time converts time to timeInMilli
        if (timeInMilli - System.currentTimeMillis() in 1..TWENTY_FOUR_HOURS_IN_MILLI) {
            scheduleAlarm(timeInMilli, todo)
        }
    }

    private fun scheduleForLastSaturday(todo: Todo) {
        val hour = todo.alarmTimeByUser.substringBefore(":").toInt()
        val minute = todo.alarmTimeByUser.substringBeforeLast(":").substringAfter(":").toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 4)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)


        val timeInMilli = calendar.time.time    // second time converts time to timeInMilli
        if (timeInMilli - System.currentTimeMillis() in 1..TWENTY_FOUR_HOURS_IN_MILLI) {
            scheduleAlarm(timeInMilli, todo)
        }
    }

    private fun scheduleForLastSunday(todo: Todo) {
        val hour = todo.alarmTimeByUser.substringBefore(":").toInt()
        val minute = todo.alarmTimeByUser.substringBeforeLast(":").substringAfter(":").toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 4)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)


        val timeInMilli = calendar.time.time    // second time converts time to timeInMilli
        if (timeInMilli - System.currentTimeMillis() in 1..TWENTY_FOUR_HOURS_IN_MILLI) {
            scheduleAlarm(timeInMilli, todo)
        }
    }

    private fun scheduleAlarm(timeInMilli: Long, todo: Todo) {

        UptoddAlarm.setAlarm(
            context,
            timeInMilli,
            todo.id,
            todo.task
        )

        todo.isAlarmSet = true

        ioScope.launch {
            todoDatabase.update(todo)
        }
    }
}



