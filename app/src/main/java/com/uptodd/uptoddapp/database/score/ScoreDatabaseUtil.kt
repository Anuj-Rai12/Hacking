package com.uptodd.uptoddapp.database.score

import com.uptodd.uptoddapp.database.todo.Todo

const val DAILY_TODO: Int = 1
const val WEEKLY_TODO: Int = 2
const val MONTHLY_TODO: Int = 3
const val ESSENTIALS_TODO: Int = 4

const val TYPE_HEADER = 65
val dosHeader = Todo(
    dosHeader = true,
    doType = TYPE_HEADER,
    alarmTime = "",
    alarmTimeInMilli = 0,
    date = "",
    imageUrl = "",
    period = -1,
    isAlarmSet = false,
    task = "",
    type = 0
)
val dontsHeader = Todo(
    dontsHeader = true,
    doType = TYPE_HEADER,
    alarmTime = "",
    alarmTimeInMilli = 0,
    date = "",
    imageUrl = "",
    period = -1,
    isAlarmSet = false,
    task = "",
    type = 0
)


fun convertToStringFromInt(todoType: Int): String {
    return when (todoType) {
        DAILY_TODO -> "daily"
        WEEKLY_TODO -> "weekly"
        MONTHLY_TODO -> "monthly"
        ESSENTIALS_TODO -> "essentials"
        else -> "null"
    }
}

const val FLAG_OLD_TODO = 55
const val FLAG_NEW_TODO = 66

