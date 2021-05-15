package com.uptodd.uptoddapp.database.todo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class Todo(

    // basic to-do info

    @ColumnInfo(name = "date")    // date when to-do was fetched from the database
    var date: String,

    @ColumnInfo(name = "task")
    var task: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    @ColumnInfo(name = "period")
    val period: Int,

    @ColumnInfo(name = "is_completed")   // initially every To-do will be set to incomplete
    var isCompleted: Boolean = false,

    @ColumnInfo(name = "is_alarm_set")
    var isAlarmSet: Boolean,

    @ColumnInfo(name = "alarm_time")
    var alarmTime: String,

    @ColumnInfo(name = "alarm_time_in_milli")
    val alarmTimeInMilli: Long,

    @ColumnInfo(name = "type")
    val type: Int,

    @ColumnInfo(name = "do_type")
    val doType: Int,

    @ColumnInfo(name = "dos_header")
    var dosHeader: Boolean = false,

    @ColumnInfo(name = "donts_header")
    var dontsHeader: Boolean = false,

    @ColumnInfo(name = "is_alarm_needed_by_user") // initially, when To-do is fetched from the database, set its on/off state to default state coming from the backend
    var isAlarmNeededByUser: Boolean = isAlarmSet,  // can eventually be changed by the user

    @ColumnInfo(name = "alarm_time_by _user") // similarly as above. Can be changed by user.
    var alarmTimeByUser: String = alarmTime,

    @ColumnInfo(name = "alarm_time_by_user_in_milli") // similarly as above. Can be changed by user.
    var alarmTimeByUserInMilli: Long = alarmTimeInMilli,

    // for weekly alarms only

    @ColumnInfo(name = "weekly_monday")
    var weeklyMonday: Boolean = false,

    @ColumnInfo(name = "weekly_tuesday")
    var weeklyTuesday: Boolean = false,

    @ColumnInfo(name = "weekly_wednesday")
    var weeklyWednesday: Boolean = false,

    @ColumnInfo(name = "weekly_thursday")
    var weeklyThursday: Boolean = false,

    @ColumnInfo(name = "weekly_friday")
    var weeklyFriday: Boolean = false,

    @ColumnInfo(name = "weekly_saturday")
    var weeklySaturday: Boolean = false,

    @ColumnInfo(name = "weekly_sunday")
    var weeklySunday: Boolean = false,

    @ColumnInfo(name = "last_swipe_date")
    var lastSwipeDate: String = "null",

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)