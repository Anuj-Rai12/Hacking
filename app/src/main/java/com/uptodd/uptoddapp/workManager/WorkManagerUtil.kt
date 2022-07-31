package com.uptodd.uptoddapp.workManager

import android.app.Application
import android.content.Context
import androidx.work.WorkManager
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences

const val DAILY_WORK_MANAGER_TAG = "dailyAlarmWorkManagerTag"
const val WEEKLY_WORK_MANAGER_TAG = "WeeklyAlarmWorkManagerTag"
const val MONTHLY_AND_ESSENTIALS_WORK_MANAGER_TAG = "MonthlyAndEssentialsAlarmWorkManagerTag"
const val DAILYCHECK_WORK_MANAGER_TAG = "DailyCheckWorkManagerTag"
const val DAILY_PRE_SALES_CHECK_WORK_MANAGER_TAG = "DailyPreSalesCheckWorkManagerTag"
const val DAILY_UP_CHECK_WORK_MANAGER="DailyUpgradeWorkManager"
const val FREE_PARENTING_PROGRAM="FreeParentingMusicVideoUpdate"
const val DAILY_SUBSCRIPTION_CHECK_WORK_MANAGER_TAG = "DailySubscriptionCheckWorkManagerTag"


fun WorkManager.cancelUptoddWorker() {
    cancelAllWorkByTag(DAILY_WORK_MANAGER_TAG)
    cancelAllWorkByTag(WEEKLY_WORK_MANAGER_TAG)
    cancelAllWorkByTag(MONTHLY_AND_ESSENTIALS_WORK_MANAGER_TAG)
    cancelAllWorkByTag(DAILYCHECK_WORK_MANAGER_TAG)
    cancelAllWorkByTag(DAILY_SUBSCRIPTION_CHECK_WORK_MANAGER_TAG)
    cancelAllWorkByTag(FREE_PARENTING_PROGRAM)
}

fun cancelAllWorkManagers(application: Application, context: Context) {
    WorkManager.getInstance(application).cancelAllWorkByTag(DAILY_WORK_MANAGER_TAG)
    WorkManager.getInstance(application).cancelAllWorkByTag(WEEKLY_WORK_MANAGER_TAG)
    WorkManager.getInstance(application).cancelAllWorkByTag(MONTHLY_AND_ESSENTIALS_WORK_MANAGER_TAG)
    WorkManager.getInstance(application).cancelAllWorkByTag(DAILYCHECK_WORK_MANAGER_TAG)
    WorkManager.getInstance(application).cancelAllWorkByTag(FREE_PARENTING_PROGRAM)
    WorkManager.getInstance(application)
        .cancelAllWorkByTag(DAILY_SUBSCRIPTION_CHECK_WORK_MANAGER_TAG)

    UptoddSharedPreferences.getInstance(context).setWorkManagerFiredStatusFalse()
}

