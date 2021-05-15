package com.uptodd.uptoddapp.workManager.updateApiWorkmanager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.database.todo.TodoDatabaseDao
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UpdateAlarmThroughApiWorker(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private lateinit var todoDatabase: TodoDatabaseDao

    override suspend fun doWork(): Result = coroutineScope {

        todoDatabase = UptoddDatabase.getInstance(context).todoDatabaseDao

        launch {
            Log.d("updatealarmworker", "launching api")
            val updateTodoId = inputData.getInt("todoToUpdate", -1)
            if (updateTodoId == -1) Result.failure()
            else {
                val todoToUpdate = todoDatabase.getTodo(updateTodoId)
                if (todoToUpdate == null) return@launch
                val todoDaysString = todoDaysToString(todoToUpdate)
                val isAlarmNeededByUser = getBooleanAsInt(todoToUpdate.isAlarmNeededByUser)

                val okHttpClient = OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(30, TimeUnit.SECONDS)
                    .build()

                AndroidNetworking.initialize(context, okHttpClient)

                val userId = getUserId(context)

                val jsonObject = JSONObject()
                jsonObject.put("userId", userId)
                jsonObject.put("activityId", todoToUpdate.id)
                jsonObject.put("alarmOnOff", isAlarmNeededByUser)
                jsonObject.put("alarmTime", todoToUpdate.alarmTimeByUser)
                jsonObject.put("alarmDays", todoDaysString)

                AndroidNetworking.put("https://uptodd.com/api/activity/alarmtime")
                    .addHeaders("Authorization", "Bearer ${getHeaderToken(context)}")
                    .addJSONObjectBody(jsonObject)
                    .setPriority(Priority.HIGH)
                    .addHeaders("Content-Type", "application/json")
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {

                            if (response != null) {
                                Log.d("updateAlarm", response.get("message").toString())
                                Log.d("updateAlarm", "$jsonObject")
                            } else {
                                Log.d("alarm api response", "response is null")
                            }
                            Result.success()
                        }

                        override fun onError(anError: ANError?) {
                            Log.d("alarm api response", anError?.message.toString())

                            Result.retry()
                        }
                    })


            }


        }

        Result.success()
    }

    private fun todoDaysToString(todo: Todo): String {
        val monday = getBooleanAsInt(todo.weeklyMonday)
        val tuesday = getBooleanAsInt(todo.weeklyTuesday)
        val wednesday = getBooleanAsInt(todo.weeklyWednesday)
        val thursday = getBooleanAsInt(todo.weeklyThursday)
        val friday = getBooleanAsInt(todo.weeklyFriday)
        val saturday = getBooleanAsInt(todo.weeklySaturday)
        val sunday = getBooleanAsInt(todo.weeklySunday)


        return "$sunday,$monday,$tuesday,$wednesday,$thursday,$friday,$saturday,$sunday"
    }

    private fun getBooleanAsInt(bool: Boolean): Int {
        return if (bool) 1 else 0
    }

    fun getUserId(context: Context): Int? {
        var preferences: SharedPreferences? = null

        preferences = context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        if (preferences!!.contains("uid"))
            return preferences.getString("uid", null)?.toInt()
        else return null
    }

    private fun getHeaderToken(context: Context): String? {
        val preferences = context.getSharedPreferences("LOGIN_INFO", AppCompatActivity.MODE_PRIVATE)
        if (preferences!!.contains("token"))
            return preferences.getString("token", "")
        else
            return null
    }


}

