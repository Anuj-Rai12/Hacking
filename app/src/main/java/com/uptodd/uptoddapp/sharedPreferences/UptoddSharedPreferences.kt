package com.uptodd.uptoddapp.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import com.uptodd.uptoddapp.database.account.Account
import com.uptodd.uptoddapp.database.logindetails.DoctorLoginInfo
import com.uptodd.uptoddapp.database.logindetails.UserInfo


@Suppress("UNCHECKED_CAST")
class UptoddSharedPreferences private constructor(context: Context) {

    companion object {

        private lateinit var uptoddSharedPreferences: UptoddSharedPreferences

        fun getInstance(context: Context): UptoddSharedPreferences {
            if (!this::uptoddSharedPreferences.isInitialized) {
                uptoddSharedPreferences = UptoddSharedPreferences(context)
            }
            return uptoddSharedPreferences
        }
    }

    // workmanager fire preference
    private val workManagerPreference: SharedPreferences by lazy {
        context.getSharedPreferences("WorkManagerFired", Context.MODE_PRIVATE)
    }

    // score preference
    private val scoreDatabasePreference: SharedPreferences by lazy {
        context.getSharedPreferences("ScoreDatabasePreference", Context.MODE_PRIVATE)
    }

    // todos refresh preference
    private val todosRefreshPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("TODO_REFRESH", Context.MODE_PRIVATE)
    }

    // Login prefrence
    private val loginSharedPreference: SharedPreferences by lazy {
        context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
    }


    // account preference
    private val accountSharedPreference: SharedPreferences by lazy {
        context.getSharedPreferences("ACCOUNT_INFO", Context.MODE_PRIVATE)
    }

    // blog category preference
    private val blogCategoryPreference: SharedPreferences by lazy {
        context.getSharedPreferences("BLOG_CAT", Context.MODE_PRIVATE)
    }

    // webinar category
    private val webinarCategoryPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("WEBINAR_CAT", Context.MODE_PRIVATE)
    }

    // last updated
    private val lastUpdatedPreferences by lazy {
        context.getSharedPreferences("last_updated", Context.MODE_PRIVATE)
    }

    fun getWorkManagerFiredStatus(): Boolean {
        return workManagerPreference.getBoolean("preferenceWorkManagerFired", false)
    }


    fun setWorkManagerFiredStatusTrue() {
        val editor = workManagerPreference.edit()
        editor.putBoolean("preferenceWorkManagerFired", true)
        editor.apply()
    }

    fun setWorkManagerFiredStatusFalse() {
        val editor = workManagerPreference.edit()
        editor.putBoolean("preferenceWorkManagerFired", false)
        editor.apply()
    }

    fun getScoreDatabaseCreatedStatus(): Boolean {
        return scoreDatabasePreference.getBoolean("preferenceScoreDatabaseCreated", false)
    }

    fun setScoreDatabaseCreatedStatusTrue() {
        val editor = scoreDatabasePreference.edit()
        editor.putBoolean("preferenceScoreDatabaseCreated", true)
        editor.apply()
    }

    fun setScoreDatabaseCreatedStatusFalse() {
        val editor = scoreDatabasePreference.edit()
        editor.putBoolean("preferenceScoreDatabaseCreated", false)
        editor.apply()
    }

    fun getLastDailyTodoFetchedDate(): String? {
        return todosRefreshPreferences.getString("DailyTodosFresh", null)
    }

    fun setLastDailyTodoFetchedDate(date: String) {
        val editor = todosRefreshPreferences.edit()
        editor.putString("DailyTodosFresh", date)
        editor.apply()
    }

    fun getLastWeeklyTodoFetchedDate(): String? {
        return todosRefreshPreferences.getString("WeeklyTodosFresh", null)
    }

    fun setLastWeeklyTodoFetchedDate(date: String) {
        val editor = todosRefreshPreferences.edit()
        editor.putString("WeeklyTodosFresh", date)
        editor.apply()
    }

    fun getLastMonthlyAndEssentialsTodoFetchedDate(): String? {
        return todosRefreshPreferences.getString(
            "monthlyAndEssentialsTodosFresh", null
        )
    }

    fun setLastMonthlyAndEssentialsTodoFetchedDate(date: String) {
        val editor = todosRefreshPreferences.edit()
        editor.putString("monthlyAndEssentialsTodosFresh", date)
        editor.apply()
    }

    fun saveLoginInfo(userInfo: UserInfo) {
        val editor = loginSharedPreference.edit()

        editor.putString(userInfo::uid.name, userInfo.uid)
        editor.putBoolean(userInfo::isNewUser.name, userInfo.isNewUser)
        editor.putString(userInfo::userType.name, userInfo.userType)
        editor.putString(userInfo::email.name, userInfo.email)
        editor.putString(userInfo::loginMethod.name, userInfo.loginMethod)
        editor.putString(userInfo::kidsDob.name, userInfo.kidsDob)
        editor.putString(userInfo::babyName.name, userInfo.babyName)
        editor.putLong(userInfo::babyDOB.name, userInfo.babyDOB)
        editor.putString(userInfo::profileImageUrl.name, userInfo.profileImageUrl)
        editor.putString(userInfo::tokenHeader.name, userInfo.tokenHeader)
        editor.putString(userInfo::parentType.name, userInfo.parentType)
        editor.putBoolean(userInfo::isPaid.name, userInfo.isPaid)
        editor.putLong(userInfo::loginTime.name, userInfo.loginTime)
        editor.putString(userInfo::token.name, userInfo.token)
        editor.putBoolean(userInfo::loggedIn.name, userInfo.loggedIn)

        editor.apply()
    }

    fun clearLoginInfo() {
        loginSharedPreference.edit().clear().apply()
    }

    fun saveDoctorLoginInfo(doctorInfo: DoctorLoginInfo) {
        val editor = loginSharedPreference.edit()

        editor.putString(doctorInfo::uid.name, doctorInfo.uid)
        editor.putString(doctorInfo::email.name, doctorInfo.email)
        editor.putString(doctorInfo::userType.name, doctorInfo.userType)
        editor.putString(doctorInfo::token.name, doctorInfo.token)
        editor.putBoolean(doctorInfo::loggedIn.name, doctorInfo.loggedIn)

        editor.apply()
    }

    fun saveAccountDetails(accountInfo: Account) {
        val editor = accountSharedPreference.edit()

        editor.putString(Account::profileImageURL.name, accountInfo.profileImageURL)
        editor.putString(Account::name.name, accountInfo.name)
        editor.putString(Account::email.name, accountInfo.email)
        editor.putString(Account::phone.name, accountInfo.phone)
        editor.putString(Account::address.name, accountInfo.address)
        editor.putInt(Account::score.name, accountInfo.score)
        editor.putInt(Account::totalScore.name, accountInfo.totalScore)
        editor.putBoolean(Account::isNannyMode.name, accountInfo.isNannyMode)
        editor.putString(Account::nannyModeUserID.name, accountInfo.nannyModeUserID)
        editor.putString(Account::nannyModePassword.name, accountInfo.nannyModePassword)
        editor.putString(Account::financeMailId.name, accountInfo.financeMailId)
        editor.putString(Account::kidsDob.name, accountInfo.kidsDob)
        editor.putString(Account::kidsName.name, accountInfo.kidsName)
        editor.putString(Account::kidsGender.name, accountInfo.kidsGender)
        editor.putString(Account::kidsPhoto.name, accountInfo.kidsPhoto)
        editor.putString(Account::whichParent.name, accountInfo.whichParent)
        editor.putString(Account::motherStage.name, accountInfo.motherStage)
        editor.putLong(Account::freeSessionAvailable.name, accountInfo.freeSessionAvailable)
        editor.putLong(Account::paidSessionCount.name, accountInfo.paidSessionCount)
        editor.putLong(Account::currentSubscribedPlan.name, accountInfo.currentSubscribedPlan)
        editor.putBoolean(Account::subscriptionActive.name, accountInfo.subscriptionActive)
        editor.putString(Account::subscriptionStartDate.name, accountInfo.subscriptionStartDate)

        editor.apply()
    }

    fun getAccountDetails(): Account {
        val account = Account()

        account.profileImageURL =
            accountSharedPreference.getString(Account::profileImageURL.name, null)
        account.name = accountSharedPreference.getString(Account::name.name, null)
        account.email = accountSharedPreference.getString(Account::email.name, null)
        account.phone = accountSharedPreference.getString(Account::phone.name, null)
        account.address = accountSharedPreference.getString(Account::address.name, null)
        account.score = accountSharedPreference.getInt(Account::score.name, -1)
        account.totalScore = accountSharedPreference.getInt(Account::totalScore.name, -1)
        account.isNannyMode = accountSharedPreference.getBoolean(Account::isNannyMode.name, false)
        account.nannyModeUserID =
            accountSharedPreference.getString(Account::nannyModeUserID.name, null)
        account.nannyModePassword =
            accountSharedPreference.getString(Account::nannyModePassword.name, null)

        account.financeMailId = accountSharedPreference.getString(Account::financeMailId.name, null)
        account.kidsDob = accountSharedPreference.getString(Account::kidsDob.name, null)
        account.kidsName = accountSharedPreference.getString(Account::kidsName.name, null)
        account.kidsGender = accountSharedPreference.getString(Account::kidsGender.name, null)
        account.kidsPhoto = accountSharedPreference.getString(Account::kidsPhoto.name, null)
        account.whichParent = accountSharedPreference.getString(Account::whichParent.name, null)
        account.motherStage = accountSharedPreference.getString(Account::motherStage.name, null)
        account.freeSessionAvailable =
            accountSharedPreference.getLong(Account::freeSessionAvailable.name, -1L)
        account.paidSessionCount =
            accountSharedPreference.getLong(Account::paidSessionCount.name, -1L)
        account.currentSubscribedPlan =
            accountSharedPreference.getLong(Account::currentSubscribedPlan.name, -1L)
        account.subscriptionActive =
            accountSharedPreference.getBoolean(Account::subscriptionActive.name, false)
        account.subscriptionStartDate = accountSharedPreference.getString(
            Account::subscriptionStartDate.name,
            null
        )

        return account
    }

    fun clearAllPreferences() {
        loginSharedPreference.edit().clear().apply()
        accountSharedPreference.edit().clear().apply()
        scoreDatabasePreference.edit().clear().apply()
        todosRefreshPreferences.edit().clear().apply()
        workManagerPreference.edit().clear().apply()
        blogCategoryPreference.edit().clear().apply()
        webinarCategoryPreferences.edit().clear().apply()
        lastUpdatedPreferences.edit().clear().apply()
    }

}