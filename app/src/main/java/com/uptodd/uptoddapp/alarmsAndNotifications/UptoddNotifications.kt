package com.uptodd.uptoddapp.alarmsAndNotifications

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.DailyActivityStatusCheck
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.NotificationBroadcastReceiver
import com.uptodd.uptoddapp.utilities.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class UptoddNotifications {
    companion object {
        fun setWishNotification(context: Context, time: Long, age: String) {
            val stringIntentExtras = HashMap<String, String>()
            stringIntentExtras["notificationTitle"] = "Happy Birthday"
            stringIntentExtras["notificationText"] =
                "Hey ${AllUtil.getBabyName()}, congratulations on your $age! Wish you a great future"
            stringIntentExtras["notificationChannelId"] = "wishes"
            stringIntentExtras["activityIntent"] = "happy_birthday"

            val intIntentExtras = HashMap<String, Int>()
            intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
            intIntentExtras["notificationId"] = WISHES_REQUEST_CODE

            val boolIntentExtras = HashMap<String, Boolean>()
            boolIntentExtras["hasActivityIntent"] = true

            UptoddNotificationUtilities.setAlarm(
                context,
                time,
                WISHES_REQUEST_CODE,
                broadcastReceiver = NotificationBroadcastReceiver::class.java,
                intentIntegerExtras = intIntentExtras,
                intentStringExtras = stringIntentExtras,
                intentBoolExtras = boolIntentExtras
            )
        }

        fun setWeeklyReferNotification(context: Context, time: Long) {
            val stringIntentExtras = HashMap<String, String>()
            stringIntentExtras["notificationTitle"] = "Refer & Earn"
            stringIntentExtras["notificationText"] =
                "Refer and earn INR 5000 or even more for every person who joins UpTodd, mom/dad the gift amount is good ;)"
            stringIntentExtras["notificationChannelId"] = "others"

            val intIntentExtras = HashMap<String, Int>()
            intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
            intIntentExtras["notificationId"] = WEEKLY_REFER_ALARM_REQUEST_CODE

            UptoddNotificationUtilities.setAlarm(
                context,
                time,
                WEEKLY_REFER_ALARM_REQUEST_CODE,
                broadcastReceiver = NotificationBroadcastReceiver::class.java,
                intentIntegerExtras = intIntentExtras,
                intentStringExtras = stringIntentExtras
            )
        }

        fun setPreSalesNotification(context: Context, time: Long, title: String, text: String) {
            val stringIntentExtras = HashMap<String, String>()
            stringIntentExtras["notificationTitle"] = title
            stringIntentExtras["notificationText"] = text
            stringIntentExtras["notificationChannelId"] = "others"

            val intIntentExtras = HashMap<String, Int>()
            intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
            intIntentExtras["notificationId"] = PRE_SALES_CODE

            UptoddNotificationUtilities.setAlarm(
                context,
                time,
                PRE_SALES_CODE,
                broadcastReceiver = NotificationBroadcastReceiver::class.java,
                intentIntegerExtras = intIntentExtras,
                intentStringExtras = stringIntentExtras
            )
        }

        fun setWeeklyPhotoNotification(context: Context, time: Long) {
            val stringIntentExtras = HashMap<String, String>()
            stringIntentExtras["notificationTitle"] = "Generate Card"
            stringIntentExtras["notificationText"] =
                "Hey Mom/Dad, generate photos by cards, at UpTodd, and share them with everyone."
            stringIntentExtras["notificationChannelId"] = "others"

            val intIntentExtras = HashMap<String, Int>()
            intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
            intIntentExtras["notificationId"] = WEEKLY_PHOTO_ALARM_REQUEST_CODE

            UptoddNotificationUtilities.setAlarm(
                context,
                time,
                WEEKLY_PHOTO_ALARM_REQUEST_CODE,
                broadcastReceiver = NotificationBroadcastReceiver::class.java,
                intentIntegerExtras = intIntentExtras,
                intentStringExtras = stringIntentExtras
            )
        }

        fun setSubscriptionEndingNotification(context: Context, time: Long) {
            var endingTime : Long

            val userId = AllUtil.getUserId()

            AndroidNetworking.get("https://uptodd.com/api/appusers/checksubscription/{userId}")
                .addPathParameter("userId", userId.toString())
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null && response.get("status") == "Success") {
                            val endingDate =
                                (response.get("data") as JSONObject).getString("subscriptionEndingDate")
                            val calendar = Calendar.getInstance()
                            calendar.set(
                                endingDate.substring(0, 4).toInt(),
                                endingDate.substring(5, 7).toInt(),
                                endingDate.substring(8, 10).toInt()
                            )
                            endingTime = calendar.timeInMillis


                            val calendarInstance = Calendar.getInstance()
                            val daysLeft = endingTime - calendarInstance.get(Calendar.DAY_OF_YEAR)
                            val stringIntentExtras = HashMap<String, String>()
                            stringIntentExtras["notificationTitle"] = "Uptodd Subscription"
                            var notificationText = ""
                            if (daysLeft > 0) {
                                when {
                                    daysLeft in 1..5 || daysLeft == 10L -> notificationText =
                                        "Hey Mom/Dad, my subscription is ending in $daysLeft days. UpTodd gave me a 10% gift, extend now."
                                    daysLeft == 30L -> notificationText =
                                        "Hey Mom/Dad, my subscription is ending in the next 30 days, extend it now."
                                }
                            } else {
                                val daysPassed = daysLeft * -1
                                if (daysPassed == 0L)
                                    notificationText =
                                        "Hey Mom/Dad, I wanna be in top-1% future leaders don’t delay subscribe for me."
                                else {
                                    when {
                                        daysPassed in 1..3 -> notificationText =
                                            "Hey Mom/Dad, its $daysPassed day I need UpTodd to become future top-1%. Join me now,  I have a 10% gift, valid for today only."

                                        daysPassed == 30L -> notificationText =
                                            " Hey Mom/Dad, its $daysPassed day I need UpTodd to become future top-1%. Join me now, I have a 10% gift."

                                        daysPassed == 7L || daysPassed == 15L -> notificationText =
                                            "Hey Mom/Dad, its $daysPassed day I need UpTodd to become future top-1%. Join me now\n"
                                    }
                                }
                            }
                            //If the user has to be notified
                            if (notificationText != "") {
                                stringIntentExtras["notificationChannelId"] = "subscription"
                                stringIntentExtras["notificationText"] = notificationText

                                val intIntentExtras = HashMap<String, Int>()
                                intIntentExtras["notificationPriority"] =
                                    NotificationCompat.PRIORITY_DEFAULT
                                intIntentExtras["notificationId"] = SUBSCRIPTION_NOTIFY_CODE

                                UptoddNotificationUtilities.setAlarm(
                                    context,
                                    time,
                                    SUBSCRIPTION_NOTIFY_CODE,
                                    broadcastReceiver = NotificationBroadcastReceiver::class.java,
                                    intentIntegerExtras = intIntentExtras,
                                    intentStringExtras = stringIntentExtras
                                )
                            }


                            Log.d("div", "AllUtil L291 $endingTime")
                        }
                    }

                    override fun onError(error: ANError?) {
                        Log.d("div", "AllUtil L294 $error")
                        if (error!!.errorCode != 0) {
                            Log.d("div", "onError errorCode : " + error.errorCode)
                            Log.d("div", "onError errorBody : " + error.errorBody)
                            Log.d("div", "onError errorDetail : " + error.errorDetail)
                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("div", "onError errorDetail : " + error.errorDetail)
                        }
                    }

                })

        }

        fun checkDailyActivityStatus(context: Context, time: Long) {
            UptoddNotificationUtilities.setAlarm(
                context,
                time,
                DAILY_ACTIVITY_STATUS_CHECK,
                DailyActivityStatusCheck::class.java
            )
        }

        fun setDoctorDailyReferralReminderCheck(days: Int, time: Long, context: Context) {
            val stringIntentExtras = HashMap<String, String>()
            stringIntentExtras["notificationTitle"] = "We miss you doctor"
            stringIntentExtras["notificationText"] =
                "Refer patients to earn with UpTodd, doctors take pride in UpTodd, its $days day no referral."
            stringIntentExtras["notificationChannelId"] = "others"
            stringIntentExtras["activityIntent"] = "we_miss_you"

            val intIntentExtras = HashMap<String, Int>()
            intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
            intIntentExtras["notificationId"] = WE_MISS_YOU_DOCTOR

            val boolIntentExtras = HashMap<String, Boolean>()
            boolIntentExtras["hasActivityIntent"] = true

            UptoddNotificationUtilities.setAlarm(
                context,
                time,
                WE_MISS_YOU_DOCTOR,
                broadcastReceiver = NotificationBroadcastReceiver::class.java,
                intentIntegerExtras = intIntentExtras,
                intentStringExtras = stringIntentExtras,
                intentBoolExtras = boolIntentExtras
            )
        }

        fun setDoctorFirstReferralReminder(time: Long, context: Context) {
            val stringIntentExtras = HashMap<String, String>()
            stringIntentExtras["notificationTitle"] = "Refer Patients"
            stringIntentExtras["notificationText"] =
                "Refer patients it’s daily, 100+ doctors trust UpTodd, refer to create an impact on the future generation"
            stringIntentExtras["notificationChannelId"] = "others"

            val intIntentExtras = HashMap<String, Int>()
            intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
            intIntentExtras["notificationId"] = DOCTOR_NO_REFERRAL

            UptoddNotificationUtilities.setAlarm(
                context,
                time,
                DOCTOR_NO_REFERRAL,
                broadcastReceiver = NotificationBroadcastReceiver::class.java,
                intentIntegerExtras = intIntentExtras,
                intentStringExtras = stringIntentExtras
            )
        }


    }
}