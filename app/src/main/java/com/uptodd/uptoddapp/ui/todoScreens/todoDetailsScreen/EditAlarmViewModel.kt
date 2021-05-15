package com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.DAILY_TODO
import com.uptodd.uptoddapp.database.score.ESSENTIALS_TODO
import com.uptodd.uptoddapp.database.score.MONTHLY_TODO
import com.uptodd.uptoddapp.database.score.WEEKLY_TODO
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.*
import com.uptodd.uptoddapp.workManager.updateApiWorkmanager.UpdateAlarmThroughApiWorker
import kotlinx.coroutines.launch
import java.util.*

class EditAlarmViewModel(val app: Application, val todoId: Int) : AndroidViewModel(app) {

    var todo = MutableLiveData<Todo>()
        private set

    private var timeSelected: Long? = null

    val timeString = MutableLiveData<String>()

    private var dateSelected: List<Int> = listOf()

    val dateString = MutableLiveData<String>()

    private var database = UptoddDatabase.getInstance(app).todoDatabaseDao

    init {

        Log.i("debug", "Initialized ViewModel EditAlarmViewModel")
        viewModelScope.launch {
            todo.value = database.getTodo(todoId)
            selectDefaultDate()
            selectDefaultTime()
            setDateText()
        }

    }

    private fun selectDefaultTime() {
        if (todo.value == null) return

        todo.value?.let {
            if (it.alarmTimeByUser.isNotEmpty()) {
                timeString.value = it.alarmTimeByUser
                return@let
            } else if (it.alarmTime.isNotEmpty()) {
                timeString.value = it.alarmTime
            }

            if(it.isAlarmNeededByUser) timeSelected = it.alarmTimeByUserInMilli
            else timeSelected = it.alarmTimeInMilli
        }
    }

    private fun selectDefaultDate() {
        if (todo.value == null) return

        todo.value?.let {
            val selectedDays = mutableListOf<Int>()
            if (it.weeklyMonday) selectedDays.add(MONDAY)
            if (it.weeklyTuesday) selectedDays.add(TUESDAY)
            if (it.weeklyWednesday) selectedDays.add(WEDNESDAY)
            if (it.weeklyThursday) selectedDays.add(THURSDAY)
            if (it.weeklyFriday) selectedDays.add(FRIDAY)
            if (it.weeklySaturday) selectedDays.add(SATURDAY)
            if (it.weeklySunday) selectedDays.add(SUNDAY)

            dateSelected = selectedDays
        }
    }

    fun updateAlarmThroughApiUsingWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
        data.putInt("todoToUpdate", todoId)


        val alarmUpdaterWorker =
            OneTimeWorkRequestBuilder<UpdateAlarmThroughApiWorker>()
                .setConstraints(constraints)
                .setInputData(data.build())
                .build()

        val workManager = WorkManager.getInstance(getApplication())
        workManager.enqueue(alarmUpdaterWorker)
        Log.d("details view model", "fired workmanager for alarm updation")
    }

    fun setDateText() {
        todo.value?.let {
            var dateText = ""
            if (it.weeklyMonday) dateText += "M"
            if (it.weeklyTuesday) dateText += "Tu"
            if (it.weeklyWednesday) dateText += "W"
            if (it.weeklyThursday) dateText += "Th"
            if (it.weeklyFriday) dateText += "F"
            if (it.weeklySaturday) dateText += "Sa"
            if (it.weeklySunday) dateText += "Su"

            for (i in 0 until dateText.length) {
                if (i == 0 || i == dateText.length - 1) dateString.value += dateText[i]
                else dateString.value += "," + dateText[i]
            }
        }
    }

    fun setTimeSelected(time: Long, tString: String) {
        timeSelected = time
        timeString.value = tString
    }

    fun setDaysSelected(days: List<Int>, dysString: String) {
        dateSelected = days
        dateString.value = dysString
        Log.i("debug", "$dateSelected ${dateString.value}")
    }

    fun saveAlarm() {

        if (todo.value == null) return


        if (todo.value!!.type == DAILY_TODO && (timeSelected == null || timeString.value == null)) return
        if (todo.value!!.type == WEEKLY_TODO && (dateSelected.isEmpty() || timeSelected == null || timeString.value == null)) return
        if (todo.value!!.type == MONTHLY_TODO && (timeSelected == null || timeString.value == null)) return
        if (todo.value!!.type == ESSENTIALS_TODO && (timeSelected == null || timeString.value == null)) return

        when (todo.value!!.type) {
            DAILY_TODO -> {
                todo.value!!.alarmTimeByUserInMilli = timeSelected!!
                todo.value!!.alarmTimeByUser = timeString.value!!
                todo.value!!.isAlarmNeededByUser = true
                todo.value!!.isAlarmSet = true
                UptoddAlarm.setAlarm(
                    app,
                    todo.value!!.alarmTimeByUserInMilli,
                    todo.value!!.id,
                    todo.value!!.task
                )
            }
            WEEKLY_TODO -> {
                // set for only selected and unset for unselected
                for (i in MONDAY..SUNDAY) {
                    if (i in dateSelected) {
                        Log.i("debug", "Setting up alarm for $i")
                        todo.value!!.isAlarmNeededByUser = true
                        todo.value!!.isAlarmSet = true
                        todo.value!!.alarmTimeByUser = timeString.value!!
                        todo.value!!.alarmTimeByUserInMilli = timeSelected!!
                        setFor(i)
                    } else {
                        unsetFor(i)
                    }
                }
            }
            // same for monthly and essential alarms
            MONTHLY_TODO -> {
                todo.value!!.isAlarmNeededByUser = true
                todo.value!!.isAlarmSet = true
                todo.value!!.alarmTimeByUserInMilli = timeSelected!!
                todo.value!!.alarmTimeByUser = timeString.value!!
                setForMonAndEn()
            }
            ESSENTIALS_TODO -> {
                todo.value!!.isAlarmNeededByUser = true
                todo.value!!.isAlarmSet = true
                todo.value!!.alarmTimeByUserInMilli = timeSelected!!
                todo.value!!.alarmTimeByUser = timeString.value!!
                setForMonAndEn()
            }

            else -> return
        }

        viewModelScope.launch {
            database.update(todo.value!!)
        }

        updateAlarmThroughApiUsingWorkManager()
    }


    // set alarm for given week day
    private fun setFor(day: Int) {
        if (todo.value == null) return

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeSelected!!

        when (day) {
            MONDAY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                Log.i("debug", "${calendar.time} || ${Calendar.getInstance().time}")
                todo.value!!.weeklyMonday = true
            }
            TUESDAY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
                Log.i("debug", "${calendar.time} || ${Calendar.getInstance().time}")
                todo.value!!.weeklyTuesday = true
            }
            WEDNESDAY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
                Log.i("debug", "${calendar.time} || ${Calendar.getInstance().time}")
                todo.value!!.weeklyWednesday = true
            }
            THURSDAY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                Log.i("debug", "${calendar.time} || ${Calendar.getInstance().time}")
                todo.value!!.weeklyThursday = true
            }
            FRIDAY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                Log.i("debug", "${calendar.time} || ${Calendar.getInstance().time}")
                todo.value!!.weeklyFriday = true
            }
            SATURDAY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                Log.i("debug", "${calendar.time} || ${Calendar.getInstance().time}")
                todo.value!!.weeklySaturday = true
            }
            SUNDAY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                Log.i("debug", "${calendar.time} || ${Calendar.getInstance().time}")
                todo.value!!.weeklySunday = true
            }
            else -> return
        }


        UptoddAlarm.setAlarm(app, calendar.timeInMillis, todo.value!!.id, todo.value!!.task)

    }

    // unset alarm for given week day
    private fun unsetFor(day: Int) {

        if (todo.value == null) return


        when (day) {
            MONDAY -> {
                todo.value!!.weeklyMonday = false
            }
            TUESDAY -> {
                todo.value!!.weeklyTuesday = false
            }
            WEDNESDAY -> {
                todo.value!!.weeklyWednesday = false
            }
            THURSDAY -> {
                todo.value!!.weeklyThursday = false
            }
            FRIDAY -> {
                todo.value!!.weeklyFriday = false
            }
            SATURDAY -> {
                todo.value!!.weeklySaturday = false
            }
            SUNDAY -> {
                todo.value!!.weeklySunday = false
            }
            else -> return
        }


        UptoddAlarm.cancelAlarm(app, todo.value!!.id, todo.value!!.task)

    }

    private fun setForMonAndEn() {
        if (todo.value == null) return

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeSelected!!

        // setup for first monday
        calendar.set(Calendar.WEEK_OF_MONTH, 1)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        UptoddAlarm.setAlarm(app, calendar.timeInMillis, todo.value!!.id, todo.value!!.task)

        // setup for second monday
        calendar.set(Calendar.WEEK_OF_MONTH, 2)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        UptoddAlarm.setAlarm(app, calendar.timeInMillis, todo.value!!.id, todo.value!!.task)

        // setup for last saturday
        calendar.set(Calendar.WEEK_OF_MONTH, 4)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        UptoddAlarm.setAlarm(app, calendar.timeInMillis, todo.value!!.id, todo.value!!.task)

        // setup for last sunday
        calendar.set(Calendar.WEEK_OF_MONTH, 4)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        UptoddAlarm.setAlarm(app, calendar.timeInMillis, todo.value!!.id, todo.value!!.task)


        todo.value!!.weeklyMonday = false
        todo.value!!.weeklyTuesday = false
        todo.value!!.weeklyWednesday = false
        todo.value!!.weeklyThursday = false
        todo.value!!.weeklyFriday = false
        todo.value!!.weeklySaturday = false
        todo.value!!.weeklySunday = false

    }

}