package com.uptodd.uptoddapp.workManager

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddNotifications
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.NotificationBroadcastReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.DailyNotificationsReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.EssentialsNotificationsReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.MonthlyNotificationsReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.WeeklyNotificationsReceiver
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.coroutineScope
import java.util.*

class DailyCheck(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {


    private fun checkDailyActivityStatus() {
        var cal = Calendar.getInstance()
        cal = AllUtil.getUntilNextHour(0, cal)
        cal.set(Calendar.MINUTE, 0)
        UptoddNotifications.checkDailyActivityStatus(context, cal.timeInMillis)
    }

    private fun setReferralNotification() {
        var calendarInstance = Calendar.getInstance()
        calendarInstance =
            AllUtil.getUntilNextDayOfWeekAfterHour(Calendar.SUNDAY, 20, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 20)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotifications.setWeeklyReferNotification(context, calendarInstance.timeInMillis)

    }

    private fun checkPodcast()
    {

        val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Podcast", "PodcastNotification", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "new channel"
            }
            notificationManager.createNotificationChannel(channel)
        }

        var cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY,11)
        cal.set(Calendar.MINUTE,0)
        val stringIntentExtras = HashMap<String, String>()
        stringIntentExtras["notificationTitle"] = "New Podcast Added"
        stringIntentExtras["notificationText"] = "Hey Mom/Dad, Check new Podcast Added."
        stringIntentExtras["type"]="Podcast"
        stringIntentExtras["notificationChannelId"] = "Podcast"
        val intIntentExtras = HashMap<String, Int>()
        intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
        intIntentExtras["notificationId"] =60007

        UptoddNotificationUtilities.setRepeatingAlarm(context
            ,cal.timeInMillis, AlarmManager.INTERVAL_DAY,DAILY_ALARM_REQUEST_CODE,NotificationBroadcastReceiver::class.java
            ,intentStringExtras = stringIntentExtras,intentIntegerExtras = intIntentExtras)

    }

    private fun setPhotoNotification() {
        var calendarInstance = Calendar.getInstance()
        calendarInstance =
            AllUtil.getUntilNextDayOfWeekAfterHour(Calendar.SUNDAY, 18, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 18)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotifications.setWeeklyPhotoNotification(context, calendarInstance.timeInMillis)

    }


    private fun setEssentialActivityNotification() {
        var calendarInstance = Calendar.getInstance()

        calendarInstance = AllUtil.getUntilNextHour(12, calendarInstance)

        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            ESSENTIALS_ALARM_REQUEST_CODE,
            EssentialsNotificationsReceiver::class.java
        )
    }

    private fun setMonthlyActivityNotification() {
        var date=context.getSharedPreferences("LOGIN_INFO",Context.MODE_PRIVATE)
            .getInt("loginDate",1)

        var calendarInstance = Calendar.getInstance()


        calendarInstance = AllUtil.getUntilNextDayOfMonthAfterHour(date, 20, calendarInstance)

        calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            MONTHLY_ALARM_REQUEST_CODE,
            MonthlyNotificationsReceiver::class.java
        )

        when{
            date<=13 -> date+=15
            date>=16 -> date-=15
            else -> date=1
        }

        calendarInstance = AllUtil.getUntilNextDayOfMonthAfterHour(date, 20, calendarInstance)

        calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            MONTHLY_ALARM_REQUEST_CODE,
            MonthlyNotificationsReceiver::class.java
        )
    }

    private fun setWeeklyActivityNotification() {
        val dayOfWeek=context.getSharedPreferences("LOGIN_INFO",Context.MODE_PRIVATE)
            .getInt("loginDay",1)

        var calendarInstance = Calendar.getInstance()

        calendarInstance =
            AllUtil.getUntilNextDayOfWeekAfterHour(dayOfWeek, 20, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 20)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            WEEKLY_ALARM_REQUEST_CODE,
            WeeklyNotificationsReceiver::class.java
        )
    }

    private fun checkSubscription() {
        var cal = Calendar.getInstance()
        cal = AllUtil.getUntilNextHour(0, cal)
        cal.set(Calendar.MINUTE, 0)
        UptoddNotifications.setSubscriptionEndingNotification(context, cal.timeInMillis)
    }

    private fun setBirthdayWish() {
        val birthdayTime = AllUtil.getBabyBirthday()
        val birthdayCalendarInstance = Calendar.getInstance()
        birthdayCalendarInstance.timeInMillis = birthdayTime

        var calendarInstance = Calendar.getInstance()

        calendarInstance =
            AllUtil.getUntilNextDayOfMonthAfterHour(birthdayCalendarInstance.get(Calendar.DAY_OF_MONTH),
                0,
                calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 0)
        calendarInstance.set(Calendar.MINUTE, 0)

        val dayDifference =
            calendarInstance.get(Calendar.DAY_OF_YEAR) - birthdayCalendarInstance.get(Calendar.DAY_OF_YEAR)
        val noOfMonths = dayDifference / 30
        var text = ""

        val months = KidsPeriod(context).getKidsAge()
        if (months >= 12)
            text = "${months / 12} years, ${months % 12} months"
        else
            text = "$months months"
        UptoddNotifications.setWishNotification(context, calendarInstance.timeInMillis, text)
    }


    override suspend fun doWork(): Result = coroutineScope {

        if (!AllUtil.isLoggedInAsDoctor()) {
            setBirthdayWish()
            checkSubscription()
            checkDailyActivityStatus()
            setEssentialActivityNotification()
            setReferralNotification()
            setPhotoNotification()
            setDailyActivityCheck()
            setWeeklyActivityNotification()
            setMonthlyActivityNotification()
            //setSessionLeftReminder()
            checkPodcast()
        } else {
            //TODO: add doctor notifications
            setWeeklyDoctorReferralNotification()
            setDailyReferralReminderCheck()
        }

        Result.success()
    }

    private fun setSessionLeftReminder() {
        var calendarInstance = Calendar.getInstance()

        val lengthOfMonth = calendarInstance.getActualMaximum(Calendar.MONTH)

        calendarInstance.set(Calendar.DAY_OF_MONTH, lengthOfMonth-5)

        if(calendarInstance.timeInMillis>System.currentTimeMillis()) {

            calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance, true)
            calendarInstance.set(Calendar.MINUTE, 0)
            calendarInstance.set(Calendar.SECOND, 0)

            UptoddNotificationUtilities.setAlarm(
                context,
                calendarInstance.timeInMillis,
                MONTHLY_SESSIONS_CHECK_ALARM_REQUEST_CODE,
                MonthlyNotificationsReceiver::class.java
            )
        }
        else{
            calendarInstance.add(Calendar.DAY_OF_MONTH, 3)
            calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance, true)
            calendarInstance.set(Calendar.MINUTE, 0)
            calendarInstance.set(Calendar.SECOND, 0)

            UptoddNotificationUtilities.setAlarm(
               context,
                calendarInstance.timeInMillis,
                MONTHLY_SESSIONS_CHECK_ALARM_REQUEST_CODE,
                MonthlyNotificationsReceiver::class.java
            )
        }
    }

    private fun setDailyReferralReminderCheck() {
        val cal = Calendar.getInstance()
        val referralTimeCal = Calendar.getInstance()
        val pref = context.getSharedPreferences("REFERRAL", Context.MODE_PRIVATE)
        val firstReferral = pref.getBoolean("firstReferral", true)
        if(firstReferral){
            var notificationTime = Calendar.getInstance()
            notificationTime = AllUtil.getUntilNextHour(12, notificationTime)
            notificationTime.set(Calendar.MINUTE, 0)
            UptoddNotifications.setDoctorFirstReferralReminder(notificationTime.timeInMillis, context)
        }
        else {
            referralTimeCal.timeInMillis = pref.getLong("latestReferral", 0)
            val days = cal.get(Calendar.DAY_OF_YEAR) - referralTimeCal.get(Calendar.DAY_OF_YEAR)
            if (days > 3) {
                var notificationTime = Calendar.getInstance()
                notificationTime = AllUtil.getUntilNextHour(10, notificationTime)
                notificationTime.set(Calendar.MINUTE, 0)
                UptoddNotifications.setDoctorDailyReferralReminderCheck(days,
                    notificationTime.timeInMillis,
                    context)
            }
        }
    }

    private fun setWeeklyDoctorReferralNotification() {
        var calendarInstance = Calendar.getInstance()
        calendarInstance = AllUtil.getUntilNextDayOfWeekAfterHour(Calendar.SATURDAY, 12, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 12)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        val stringIntentExtras = HashMap<String, String>()
        stringIntentExtras["notificationTitle"] = "Refer Doctors"
        stringIntentExtras["notificationText"] = "Refer to your doctor friends to earn 10% flat."
        stringIntentExtras["notificationChannelId"] = "others"

        val intIntentExtras = HashMap<String, Int>()
        intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
        intIntentExtras["notificationId"] = WEEKLY_DOCTOR_REFER_ALARM

        UptoddNotificationUtilities.setAlarm(context,
            calendarInstance.timeInMillis,
            WEEKLY_REFER_ALARM_REQUEST_CODE,
            broadcastReceiver = NotificationBroadcastReceiver::class.java,
            intentIntegerExtras = intIntentExtras,
            intentStringExtras = stringIntentExtras)
    }

    private fun setDailyActivityCheck() {
        var cal = Calendar.getInstance()
        cal = AllUtil.getUntilNextHour(20, cal)
        cal.set(Calendar.MINUTE, 0)

        UptoddNotificationUtilities.setAlarm(context,
            cal.timeInMillis,
            DAILY_ALARM_REQUEST_CODE,
            DailyNotificationsReceiver::class.java)
    }

}