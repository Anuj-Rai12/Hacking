package com.uptodd.uptoddapp.workManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddNotifications
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.coroutineScope
import java.util.*

class DailyPreSalesCheck(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private fun checkPreSales(){
        val cal = Calendar.getInstance()
        val loginTimeCal = Calendar.getInstance()
        val pref = context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        val isPaid = pref.getBoolean("isPaid", false)
        loginTimeCal.timeInMillis = pref.getLong("loginTime", System.currentTimeMillis())
        if(!isPaid) {
            val days = cal.get(Calendar.DAY_OF_YEAR) - loginTimeCal.get(Calendar.DAY_OF_YEAR)
            when (days) {

                1 -> {
                    cal.set(Calendar.HOUR_OF_DAY, 10)
                    cal.set(Calendar.MINUTE, 0)
                    UptoddNotifications.setPreSalesNotification(context,
                        cal.timeInMillis,
                        "Subscribe Now",
                        "Mom/Dad, my brain is developing fast, I need the right environment. Thank you for joining UpTodd for me.")
                }
                2 -> {
                    cal.set(Calendar.HOUR_OF_DAY, 10)
                    cal.set(Calendar.MINUTE, 0)
                    UptoddNotifications.setPreSalesNotification(context,
                        cal.timeInMillis,
                        "Subscribe Now",
                        "MOM/DAD, Do you know 99% of people suffer due to bad cognitive growth.")
                }
                3 -> {
                    cal.set(Calendar.HOUR_OF_DAY, 14)
                    cal.set(Calendar.MINUTE, 0)
                    UptoddNotifications.setPreSalesNotification(context,
                        cal.timeInMillis,
                        "Subscribe Now",
                        "MOM/DAD, UpTodd can provide a safe future to me, as they curated home environment programs by 10K+ research studies by top doctors, educationists, and experts.")
                    cal.set(Calendar.HOUR_OF_DAY, 10)
                    cal.set(Calendar.MINUTE, 0)
                    UptoddNotifications.setPreSalesNotification(context,
                        cal.timeInMillis,
                        "Subscribe Now",
                        "MOM/DAD, Lakhs of parents already joined cognitive development missions for babies, I also want to join to ace in Top-1% of global top brains.")
                }
                15 -> {
                    cal.set(Calendar.HOUR_OF_DAY, 10)
                    cal.set(Calendar.MINUTE, 0)
                    UptoddNotifications.setPreSalesNotification(context,
                        cal.timeInMillis,
                        "Subscribe Now",
                        "Hey Mom/Dad, I want to be future top-1% leaders, enrol me in UpTodd.")
                }
            }
        }
    }



    override suspend fun doWork(): Result = coroutineScope {

        if (!AllUtil.isLoggedInAsDoctor()) {
            checkPreSales()
        }

        Result.success()
    }
}