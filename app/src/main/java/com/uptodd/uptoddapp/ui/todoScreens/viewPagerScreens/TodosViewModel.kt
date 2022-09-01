package com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddAlarm
import com.uptodd.uptoddapp.api.getUserId
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryBoosterFiles
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.score.*
import com.uptodd.uptoddapp.database.todo.Todo
import com.uptodd.uptoddapp.database.todoApiDatabase.FLAG_ROW_NOT_ADDED
import com.uptodd.uptoddapp.database.todoApiDatabase.UpdateApi
import com.uptodd.uptoddapp.database.webinars.Webinars
import com.uptodd.uptoddapp.helperClasses.DateClass
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.home.homePage.repo.HomPageRepository
import com.uptodd.uptoddapp.ui.home.homePage.reviewmodel.ProgramReviewRequest
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.DAILY_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.ESSENTIALS_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.MONTHLY_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.masterFragment.WEEKLY_TODOS_TAB_POSITION
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.Downloader
import com.uptodd.uptoddapp.utilities.KidsPeriod
import com.uptodd.uptoddapp.utils.RateUsSave
import com.uptodd.uptoddapp.workManager.updateApiWorkmanager.UpdateAlarmThroughApiWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.ceil

class TodosViewModel(
    database: UptoddDatabase,
    private val period: Int,
    application: Application
) :
    AndroidViewModel(application) {

    private val todoDatabase = database.todoDatabaseDao
    private val updateApiDatabase = database.updateApiDatabaseDao
    private val musicDatabase = database.musicDatabaseDao
    private val memoryDatabase = database.memoryBoosterDao

    private val homPageRepository = HomPageRepository()

    var dpi: String = ""
    var apiError: String = ""
    var isnavigated = false


    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading


    private val _getReviewSection = MutableLiveData<Pair<String, Any>>()
    val getReviewSection: LiveData<Pair<String, Any>>
        get() = _getReviewSection


    private var _isOutdatedVersion: MutableLiveData<Boolean> = MutableLiveData()
    val isOutDatedVersion: LiveData<Boolean>
        get() = _isOutdatedVersion


    private val _linkToGetKit = MutableLiveData<String>()
    val linkGetKit: LiveData<String>
        get() = _linkToGetKit


    private var _isRefreshing: MutableLiveData<Boolean> = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing

    private var _isNewUser: MutableLiveData<Boolean> = MutableLiveData()
    val isNewUser: LiveData<Boolean>
        get() = _isNewUser

    private var _webinars = MutableLiveData<ArrayList<Webinars>>()
    val webinars: LiveData<ArrayList<Webinars>>
        get() = _webinars

    private var _showDownloadingFlag = MutableLiveData<Boolean>(false)
    val showDownloadingFlag: LiveData<Boolean>
        get() = _showDownloadingFlag

    private lateinit var downloadedMusic: List<MusicFiles>
    private lateinit var downloadedMemoryMusic: List<MemoryBoosterFiles>

    var notificationIntent = MutableLiveData(0)

    var notificationIntentExtras = MutableLiveData<Bundle>()

    // ------------------------------------------------>>>>>
    //       Main source of data
    //------------------------------------------------->>>>>
    private val dailyPendingTodosListSource: LiveData<List<Todo>> =
        todoDatabase.getAllPendingTodosOfType(DAILY_TODO, period = period)

    val dailyPendingTodosList = Transformations.map(dailyPendingTodosListSource) {
        sortList(it)
    }


    fun postReviewApi(request: ProgramReviewRequest) {

        viewModelScope.launch {
            _getReviewSection.postValue(
                Pair(
                    HomPageRepository.Companion.AndroidNetworkingResponseWrapper.LOADING.name,
                    "Sending your FeedBack.."
                )
            )
            homPageRepository.postResponseItem(
                request,
                success = {
                    _getReviewSection.postValue(
                        Pair(
                            HomPageRepository.Companion.AndroidNetworkingResponseWrapper.SUCCESS.name,
                            it
                        )
                    )
                }, error = { err, msg ->
                    _getReviewSection.postValue(
                        Pair(
                            HomPageRepository.Companion.AndroidNetworkingResponseWrapper.ERROR.name,
                            err?.localizedMessage
                                ?: msg ?: "Unknown Error"
                        )
                    )
                }
            )
        }
    }

    fun initReviewSection() {
        _getReviewSection.postValue(null)
        _linkToGetKit.postValue(null)
    }


    private val weeklyPendingTodosListSource: LiveData<List<Todo>> =
        todoDatabase.getAllPendingTodosOfType(WEEKLY_TODO, period = period)

    val weeklyPendingTodosList = Transformations.map(weeklyPendingTodosListSource) {
        sortList(it)
    }

    private val monthlyPendingTodosListSource: LiveData<List<Todo>> =
        todoDatabase.getAllPendingTodosOfType(
            MONTHLY_TODO, period = period
        )

    val monthlyPendingTodosList = Transformations.map(monthlyPendingTodosListSource) {
        sortList(it)
    }

    private val essentialsPendingTodosListSource: LiveData<List<Todo>> =
        todoDatabase.getAllPendingTodosOfType(
            ESSENTIALS_TODO, period = period
        )

    val essentialsPendingTodosList = Transformations.map(essentialsPendingTodosListSource) {
        sortList(it)
    }


    // livedata for ideal height and weight
    var idealHeight = MutableLiveData<String>()
        private set

    var idealWeight = MutableLiveData<String>()
        private set

    private fun sortList(list: List<Todo>): List<Todo> {

        val finalList = arrayListOf(dosHeader)

        val dos = list.filter {
            it.doType == 1
        }

        finalList.addAll(dos)

        val donts = list.filter {
            it.doType == 0
        }


        if (donts.isEmpty()) return finalList.toList()

        finalList.add(dontsHeader)
        finalList.addAll(donts)
        return finalList.toList()
    }


    private var _tabPosition = MutableLiveData<Int>()
    val tabPosition: LiveData<Int>
        get() = _tabPosition

    private var _score = MutableLiveData<String>()
    val score: LiveData<String>
        get() = _score


    fun loadDailyTodoScore() {
        viewModelScope.launch {

            val completed = todoDatabase.getCountOfTodosCompletedOfType(DAILY_TODO, period = period)
            val total = todoDatabase.getCountOfTodosOfType(DAILY_TODO, period = period)
            _score.value = "$completed/$total"
            if (completed == total) navigateToAppreciationScreen()
            else doneNavigatingToAppreciationScreen()
            _tabPosition.value = DAILY_TODOS_TAB_POSITION
        }
    }

    fun loadWeeklyTodoScore() {
        viewModelScope.launch {
            val completed =
                todoDatabase.getCountOfTodosCompletedOfType(WEEKLY_TODO, period = period)
            val total = todoDatabase.getCountOfTodosOfType(WEEKLY_TODO, period = period)
            _score.value = "$completed/$total"
            if (completed == total) navigateToAppreciationScreen()
            else doneNavigatingToAppreciationScreen()
            _tabPosition.value = WEEKLY_TODOS_TAB_POSITION
        }
    }

    fun loadMonthlyTodoScore() {
        viewModelScope.launch {

            val completed =
                todoDatabase.getCountOfTodosCompletedOfType(MONTHLY_TODO, period = period)
            val total = todoDatabase.getCountOfTodosOfType(MONTHLY_TODO, period = period)
            _score.value = "$completed/$total"
            if (completed == total) navigateToAppreciationScreen()
            else doneNavigatingToAppreciationScreen()
            _tabPosition.value = MONTHLY_TODOS_TAB_POSITION
        }
    }

    fun loadEssentialsTodoScore() {
        viewModelScope.launch {

            val completed =
                todoDatabase.getCountOfTodosCompletedOfType(ESSENTIALS_TODO, period = period)
            val total = todoDatabase.getCountOfTodosOfType(ESSENTIALS_TODO, period = period)
            _score.value = "$completed/$total"
            if (completed == total) navigateToAppreciationScreen()
            else doneNavigatingToAppreciationScreen()
            _tabPosition.value = ESSENTIALS_TODOS_TAB_POSITION
        }

    }


    //---------------------------------------------------------------->>>
//           Navigation related functions and variables
    //---------------------------------------------------------------->>>

    private var _navigateToAppreciationScreenFlag = MutableLiveData<Boolean>()
    val navigateToAppreciationScreenFlag: LiveData<Boolean>
        get() = _navigateToAppreciationScreenFlag

    private fun navigateToAppreciationScreen() {
        _navigateToAppreciationScreenFlag.value = true
    }

    private fun doneNavigatingToAppreciationScreen() {
        _navigateToAppreciationScreenFlag.value = false
    }

    fun openDailyTodos() {
        _tabPosition.value = DAILY_TODOS_TAB_POSITION
        Log.i("tabPos", _tabPosition.value.toString())
    }

    fun openWeeklyTodos() {
        _tabPosition.value = WEEKLY_TODOS_TAB_POSITION
        Log.i("tabPos", _tabPosition.value.toString())
    }

    fun openMonthlyTodos() {
        _tabPosition.value = MONTHLY_TODOS_TAB_POSITION
        Log.i("tabPos", _tabPosition.value.toString())
    }


    //---------------------------------------------------------------->>>
//           Multiple item selection variables and functions
    //---------------------------------------------------------------->>>

    private var _multipleDailySelectionTaskCompleteFlag = MutableLiveData<Boolean>()
    val multipleDailySelectionTaskCompleteFlag: LiveData<Boolean>
        get() = _multipleDailySelectionTaskCompleteFlag

    fun multipleDailySelectionTaskComplete() {
        _multipleDailySelectionTaskCompleteFlag.value = true
    }

    private fun multipleDailySelectionTaskStarted() {
        _multipleDailySelectionTaskCompleteFlag.value = false
    }

    private var _multipleWeeklySelectionTaskCompleteFlag = MutableLiveData<Boolean>()
    val multipleWeeklySelectionTaskCompleteFlag: LiveData<Boolean>
        get() = _multipleWeeklySelectionTaskCompleteFlag

    fun multipleWeeklySelectionTaskComplete() {
        _multipleWeeklySelectionTaskCompleteFlag.value = true
    }

    private fun multipleWeeklySelectionTaskStarted() {
        _multipleWeeklySelectionTaskCompleteFlag.value = false
    }

    private var _multipleMonthlySelectionTaskCompleteFlag = MutableLiveData<Boolean>()
    val multipleMonthlySelectionTaskCompleteFlag: LiveData<Boolean>
        get() = _multipleMonthlySelectionTaskCompleteFlag

    fun multipleMonthlySelectionTaskComplete() {
        _multipleMonthlySelectionTaskCompleteFlag.value = true
    }

    private fun multipleMonthlySelectionTaskStarted() {
        _multipleMonthlySelectionTaskCompleteFlag.value = false
    }

    private var _multipleEssentialsSelectionTaskCompleteFlag = MutableLiveData<Boolean>()
    val multipleEssentialsSelectionTaskCompleteFlag: LiveData<Boolean>
        get() = _multipleEssentialsSelectionTaskCompleteFlag

    fun multipleEssentialsSelectionTaskComplete() {
        _multipleEssentialsSelectionTaskCompleteFlag.value = true
    }

    private fun multipleEssentialsSelectionTaskStarted() {
        _multipleEssentialsSelectionTaskCompleteFlag.value = false
    }

    //---------------------------------------------------------------->>>
//            Mark as complete  functions
    //---------------------------------------------------------------->>>

    fun markAllDailyAsComplete(
        todosList: ArrayList<Todo>,
        context: Context
    ) {
        viewModelScope.launch {
            multipleDailySelectionTaskStarted()
            val list = CopyOnWriteArrayList(todosList)
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val todo = iterator.next()
                val res = async(IO) {
                    todo.isCompleted = true
                    todoDatabase.update(todo)
                    addToUpdateApiDatabase(todo)
                    cancelAlarmAfterSwipe(todo, context)
                }
                res.await()
            }

            loadDailyTodoScore()

            multipleDailySelectionTaskComplete()
        }
    }

    fun markAllWeeklyAsComplete(todosList: ArrayList<Todo>, context: Context) {
        if (todosList.isEmpty())
            return
        viewModelScope.launch {
            multipleWeeklySelectionTaskStarted()
            val list = todosList.iterator()
            while (list.hasNext()) {
                val todo = list.next()
                val res = async(IO) {
                    todo.isCompleted = true
                    todoDatabase.update(todo)
                    loadWeeklyTodoScore()
                    addToUpdateApiDatabase(todo)
                    cancelAlarmAfterSwipe(todo, context)
                }
                res.await()
            }

            multipleWeeklySelectionTaskComplete()
        }
    }

    fun markAllMonthlyAsComplete(todosList: ArrayList<Todo>, context: Context) {
        if (todosList.isEmpty())
            return
        viewModelScope.launch {
            multipleMonthlySelectionTaskStarted()
            val list = todosList.iterator()
            while (list.hasNext()) {
                val todo = list.next()
                val res = async(IO) {
                    todo.isCompleted = true
                    todoDatabase.update(todo)
                    loadMonthlyTodoScore()
                    addToUpdateApiDatabase(todo)
                    cancelAlarmAfterSwipe(todo, context)
                }
                res.await()
            }

            multipleMonthlySelectionTaskComplete()
        }
    }

    fun markAllEssentialsAsComplete(todosList: ArrayList<Todo>, context: Context) {
        if (todosList.isEmpty())
            return
        viewModelScope.launch {
            multipleEssentialsSelectionTaskStarted()
            val list = todosList.iterator()
            while (list.hasNext()) {
                val todo = list.next()
                val res = async(IO) {
                    todo.isCompleted = true
                    todoDatabase.update(todo)
                    loadEssentialsTodoScore()
                    addToUpdateApiDatabase(todo)
                    cancelAlarmAfterSwipe(todo, context)
                }
                res.await()
            }

            multipleEssentialsSelectionTaskComplete()
        }
    }

    fun markAsComplete(todo: Todo, context: Context) {
        viewModelScope.launch {
            val res = async(IO) {

                todo.isCompleted = true
                todoDatabase.update(todo)

                when (todo.type) {
                    DAILY_TODO -> loadDailyTodoScore()
                    WEEKLY_TODO -> loadWeeklyTodoScore()
                    MONTHLY_TODO -> loadMonthlyTodoScore()
                    ESSENTIALS_TODO -> loadEssentialsTodoScore()
                }
                addToUpdateApiDatabase(todo)
                cancelAlarmAfterSwipe(todo, context)
            }
            res.await()
        }
    }

    //---------------------------------------------------------------->>>
//           Alarm On and Off
    //---------------------------------------------------------------->>>

    private var _alarmSetToast = MutableLiveData<Boolean>()
    val alarmSetToast: LiveData<Boolean>
        get() = _alarmSetToast

    private fun showAlarmSetNotification() {
        _alarmSetToast.value = true
    }

    fun doneShowingAlarmSetNotification() {
        _alarmSetToast.value = false
    }

    private var _alarmCancelledToast = MutableLiveData<Boolean>()
    val alarmCancelledToast: LiveData<Boolean>
        get() = _alarmCancelledToast

    private fun showAlarmCancelledNotification() {
        _alarmCancelledToast.value = true
    }

    fun doneShowingAlarmCancelledNotification() {
        _alarmCancelledToast.value = false
    }

    fun autoScheduleDailyAlarms(context: Context) {
        viewModelScope.launch {
            val dailyTodosForAutosetAlarm =
                todoDatabase.getDailyTodosForAlarmAutoset(period = period)
            for (todo in dailyTodosForAutosetAlarm) {
                val alarmTimeInMilli =
                    DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
                if (alarmTimeInMilli != null && alarmTimeInMilli > System.currentTimeMillis()) {
                    UptoddAlarm.setAlarm(
                        context,
                        alarmTimeInMilli,
                        todo.id,
                        todo.task
                    )
                    updateAlarmWasScheduledToDatabase(todo)
                    Log.d("auto", "successful schedule alarm for $todo")
                } else if (alarmTimeInMilli != null && alarmTimeInMilli < System.currentTimeMillis()) {  // in case alarm is for 9 am and it is 10am right now, we make sure alarm is not scheduled for that
                    todo.isAlarmNeededByUser = true
                    todo.isAlarmSet = false
                    todoDatabase.update(todo)
                } else {
                    Log.d("auto", "couldnt schedule alarm for $todo")
                }

            }
        }
    }

    private fun cancelAlarmAfterSwipe(todo: Todo, context: Context) {
        viewModelScope.launch {
            UptoddAlarm.cancelAlarm(context, todo.id)
            todo.isAlarmSet = false
            todo.lastSwipeDate = DateClass().getCurrentDateAsString()
            todoDatabase.update(todo)
        }

    }

    fun turnOnAlarm(todo: Todo, context: Context) {
        viewModelScope.launch {
            if (todo.isAlarmSet) return@launch  // cancel operation if alarm is already set
            val alarmTimeInMilli =
                DateClass().convertToTimeInMilliFromTimeString(todo.alarmTimeByUser)
            if (alarmTimeInMilli != null && alarmTimeInMilli > System.currentTimeMillis()) {
                UptoddAlarm.setAlarm(
                    context,
                    alarmTimeInMilli,
                    todo.id,
                    todo.task
                )
                Log.d("alarm", "todo vm L441 time: $alarmTimeInMilli")

                showAlarmSetNotification()

                todo.isAlarmSet = true
                todo.isAlarmNeededByUser = true
                todoDatabase.update(todo)
                updateAlarmThroughApiUsingWorkManager(todo.id)
            } else {
                Log.d("error", "alarmTime in milli is null for id ${todo.id}")
            }

        }
    }

    fun turnOFFAlarm(todo: Todo, context: Context) {
        viewModelScope.launch {
            if (!todo.isAlarmSet) return@launch  // cancel operation if alarm is not set

            UptoddAlarm.cancelAlarm(
                context,
                todo.id
            )  // since alarm request code is same as todoId

            showAlarmCancelledNotification()

            todo.isAlarmNeededByUser = false
            todo.isAlarmSet = false
            todoDatabase.update(todo)
            updateAlarmThroughApiUsingWorkManager(todo.id)
        }
    }

    private suspend fun updateAlarmWasScheduledToDatabase(todo: Todo) {
        withContext(IO) {
            todo.isAlarmSet = true
            todo.isAlarmNeededByUser = true
            todoDatabase.update(todo)
        }
    }

    //---------------------------------------------------------------->>>
//           Api related functions and variables
    //---------------------------------------------------------------->>>

    private var _isDataOutdatedFlag = MutableLiveData<Boolean>()
    val isDataOutdatedFlag: LiveData<Boolean>
        get() = _isDataOutdatedFlag

    fun performAction(context: Context, activity: Activity) {
        // these functions must run only once a day with internet connected
        viewModelScope.launch {
            uploadDailyTodosThroughApi(activity)
            uploadWeeklyTodosThroughApi(activity)
            uploadMonthlyTodosThroughApi(activity)
            uploadEssentialsTodosThroughApi(activity)
            refreshDataByCallingApi(context, activity)
        }
    }


    fun checkForAppUpdate(context: Context) {
        val lastCheck = UptoddSharedPreferences.getInstance(context).getLastVersionCheck()

        val calendar = Calendar.getInstance()
        Log.d("minutes", "${TimeUnit.MILLISECONDS.toHours(calendar.timeInMillis - lastCheck)}")
        //_linkToGetKit.postValue("http://www.google.com")
        if (lastCheck != 0L && TimeUnit.MILLISECONDS.toHours(calendar.timeInMillis - lastCheck) < 2) {
            _isOutdatedVersion.value = false
            return
        }
        Log.i("ANUJ", "checkForAppUpdate: Checkout for update")

        AndroidNetworking.get("https://www.uptodd.com/api/appusers/dailyChecks/${AllUtil.getUserId()}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val data = response?.get("data") as JSONObject
                    val appVersion = context.packageManager.getPackageInfo(context.packageName, 0)
                    Log.d("data", data.toString())
                    Log.i("ANUJ", "onResponse: $data")

                    val res =
                        (data.get("versionDetails") as JSONObject).getDouble("android_supported")


                    val subSubscribe = (data.get("subscriptionDetails") as JSONObject)
                        .get("subscriptionStartDate") as String

                    Log.i("ANUJ", " SubSubScribe ---> onResponse: $subSubscribe")
                    setRateUs(subSubscribe, context, true)

                    val isOnBoardingFilled = (data.get("onboardingFormDetails") as JSONObject)
                        .get("isOnboardingFormFilled") as Int

                    val isDevelopmentFormOpen = (data.get("isDevelopmentFormOpen")) as Int

                    val onboardingFormLink = (data.get("onboardingFormDetails") as JSONObject)
                        .get("onboardingFormLink") as String
                    val isSessionBookingAllowed = (data.get("sessionDetails") as JSONObject)
                        .get("isSessionBookingAllowed") as Int
                    val shouldShowKit = (data.get("kitTutorial")) as Int


                    val allowToKit = (data.get("nextKitFormDetails") as JSONObject)
                        .get("isAllowed") as Int

                    val kitLink = (data.get("nextKitFormDetails") as JSONObject)
                        .get("link") as String


                    if (allowToKit==1){
                        _linkToGetKit.postValue(kitLink)
                    }else{//0 by default
                        _linkToGetKit.postValue(null)
                    }

                    val sharedPreferences = UptoddSharedPreferences.getInstance(context)
                    sharedPreferences.setOnBoardingDetailsFilled(isOnBoardingFilled)
                    sharedPreferences.setIsSessionBookingAllowed(isSessionBookingAllowed)
                    sharedPreferences.setOnboardingLink(onboardingFormLink)
                    sharedPreferences.setShouldShowKitTutorial(shouldShowKit == 1)
                    sharedPreferences.setFillDevelopmentForm(isDevelopmentFormOpen)
                    Log.d("Fill development form", "$isDevelopmentFormOpen")
                    Log.d("data version", "$res")
                    val appVer_ceil = ceil(appVersion.versionName.toDouble())
                    val appVer_abs = abs(appVersion.versionName.toDouble())
                    val res_ceil = ceil(res)
                    val res_abs = abs(res)
                    val result = !(res_ceil <= appVer_ceil || res_abs <= appVer_abs)
                    Log.i("ANUJ", "onResponse: should show Update Screen  $result")
                    _isOutdatedVersion.value = result
                    Log.d("called version", "true")
                    UptoddSharedPreferences.getInstance(context)
                        .saveLastVersionChecked(calendar.timeInMillis)
                }

                override fun onError(anError: ANError?) {
                    Log.d("data version error", "${anError?.errorDetail}")
                    Log.i("ANUJ", "onError: ${anError?.errorDetail}")
                    _isOutdatedVersion.value = false
                }

            })
    }

    fun setRateUs(subSubscribe: String, context: Context, day15Flag: Boolean = false) {
        val dataStore = UptoddSharedPreferences.getInstance(context)
        val rateUsSave = RateUsSave(dataStore)
        val day = dataStore.getRatingDay()
        val month = dataStore.getRatingMonth()
        val type = dataStore.getRatingType()
        Log.i("RATEUS", "setRateUs: DAY is $day MONTH $month and $type")
        viewModelScope.launch {
            if (day == -1 && month == -1 && type.isNullOrEmpty() && day15Flag) {
                rateUsSave.saveForFirst15Day(subSubscribe)
            } else if (day != -1 && month != -1 && !type.isNullOrEmpty() && !day15Flag) {
                rateUsSave.saveDateOnEvery30Day(currentDay = day, currentMonth = month)
            }
        }
    }


    private suspend fun addToUpdateApiDatabase(todo: Todo) {
        viewModelScope.launch {
            withContext(IO) {
                val checkRowAvailable = updateApiDatabase.checkRowAvailable(todo.date, todo.type)

                if (checkRowAvailable == 1) {

                    val updateData = updateApiDatabase.getRow(todo.date, todo.type)
                    updateData.activityId = updateData.activityId + ",${todo.id}"
                    updateData.isUpdated = false
                    updateData.swipeDate = DateClass().getCurrentDateStringAsYYYYMMDD()
                    updateApiDatabase.update(updateData)


                } else if (checkRowAvailable == 0) {
                    // row is not available
                    updateApiDatabase.createRow(
                        UpdateApi(
                            DateClass().getCurrentDateStringAsYYYYMMDD(),
                            todo.date,
                            todo.type,
                            todo.id.toString(),
                            FLAG_ROW_NOT_ADDED,
                            false
                        )
                    )

                }
            }
        }
    }

    fun uploadDailyTodosThroughApi(activity: Activity) {
        viewModelScope.launch {
            val pendingTodos: List<UpdateApi> = updateApiDatabase.pendingTodosToUpload(DAILY_TODO)
            for (todo in pendingTodos) {
                uploadTodosUsingApiPost(todo, activity)
            }
        }
    }

    fun uploadWeeklyTodosThroughApi(activity: Activity) {
        viewModelScope.launch {
            val pendingTodos: List<UpdateApi> = updateApiDatabase.pendingTodosToUpload(WEEKLY_TODO)
            for (todo in pendingTodos) {
                if (!todo.activityId.contains(',') && todo.rowId == FLAG_ROW_NOT_ADDED) {
                    uploadTodosUsingApiPost(todo, activity)
                } else if (todo.activityId.contains(',') && todo.rowId == FLAG_ROW_NOT_ADDED) {
                    uploadTodosUsingApiPost(todo, activity)
                } else if (todo.activityId.contains(',') && todo.rowId >= 0) {
                    uploadTodosUsingApiPut(todo, activity)
                }
            }
        }
    }

    fun uploadMonthlyTodosThroughApi(activity: Activity) {
        viewModelScope.launch {
            val pendingTodos: List<UpdateApi> = updateApiDatabase.pendingTodosToUpload(MONTHLY_TODO)
            for (todo in pendingTodos) {
                if (!todo.activityId.contains(',') && todo.rowId == FLAG_ROW_NOT_ADDED) {
                    uploadTodosUsingApiPost(todo, activity)
                } else if (todo.activityId.contains(',') && todo.rowId == FLAG_ROW_NOT_ADDED) {
                    uploadTodosUsingApiPost(todo, activity)
                } else if (todo.activityId.contains(',') && todo.rowId >= 0) {
                    uploadTodosUsingApiPut(todo, activity)
                }
            }
        }
    }

    fun uploadEssentialsTodosThroughApi(activity: Activity) {
        viewModelScope.launch {
            val pendingTodos: List<UpdateApi> = updateApiDatabase.pendingTodosToUpload(
                ESSENTIALS_TODO
            )
            for (todo in pendingTodos) {
                if (!todo.activityId.contains(',') && todo.rowId == FLAG_ROW_NOT_ADDED) {
                    uploadTodosUsingApiPost(todo, activity)
                } else if (todo.activityId.contains(',') && todo.rowId == FLAG_ROW_NOT_ADDED) {
                    uploadTodosUsingApiPost(todo, activity)
                } else if (todo.activityId.contains(',') && todo.rowId >= 0) {
                    uploadTodosUsingApiPut(todo, activity)
                }
            }
        }
    }


    private fun uploadTodosUsingApiPut(updateData: UpdateApi, activity: Activity) {

        viewModelScope.launch {
            val rowId = updateData.rowId
            if (rowId == FLAG_ROW_NOT_ADDED) {
                return@launch
            }


            val userId = getUserId(activity)

            val jsonObject = JSONObject()
            jsonObject.put("userId", userId)
            jsonObject.put("id", updateData.rowId)
            jsonObject.put("activityCompleted", updateData.activityId)
            jsonObject.put("scoreDate", updateData.swipeDate)

            AndroidNetworking.put("https://www.uptodd.com/api/activity/score")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            Log.d("putting", response.get("message").toString())
                            Log.d("putting", "$jsonObject")

                            viewModelScope.launch {
                                withContext(IO) {
                                    updateData.isUpdated = true
                                    updateApiDatabase.update(updateData)
                                }
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {

                        if (anError != null) {
                            Log.d("error:", anError.message.toString())
                        }

                    }
                })
        }
    }


    private fun uploadTodosUsingApiPost(updateData: UpdateApi, activity: Activity) {
        viewModelScope.launch {

            val userId = getUserId(activity)


            val jsonObject = JSONObject()
            jsonObject.put("userId", userId)
            jsonObject.put("activityType", convertToStringFromInt(updateData.type))
            jsonObject.put("activityCompleted", updateData.activityId)
            jsonObject.put("period", period)
            jsonObject.put("scoreDate", updateData.swipeDate)

            AndroidNetworking.post("https://www.uptodd.com/api/activity/score")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            Log.d("posting", response.get("message").toString())
                            Log.d("posting", "$jsonObject")

                            viewModelScope.launch {
                                withContext(IO) {
                                    val allData = response.get("data") as JSONObject

                                    updateData.rowId = allData.getInt("newAddedRowId")
                                    updateData.isUpdated = true
                                    updateApiDatabase.update(updateData)

                                }
                            }

                        }
                    }

                    override fun onError(anError: ANError?) {

                        if (anError != null) {
                            Log.d("error:", anError.message.toString())
                        }
                    }
                })
        }
    }

    fun refreshDataByCallingApi(context: Context, activity: Activity) {
        viewModelScope.launch {
            _isDataOutdatedFlag.value = true
            _isRefreshing.value = true
            val userId = getUserId(activity)
            val language = ChangeLanguage(context).getLanguage()

            Log.d(
                "div",
                "TodosViewModel L664 ${"https://uptodd.com/api/activities?userId=$userId&period=$period&lang=$language"}"
            )

            val userType = UptoddSharedPreferences.getInstance(context).getUserType()
            val stage = UptoddSharedPreferences.getInstance(context).getStage()
            val country = AllUtil.getCountry(context)
            AndroidNetworking.get("https://www.uptodd.com/api/activities?userId=$userId&period=$period&lang=$language&userType=$userType&country=$country&motherStage=$stage")        //replace music by blog in L54 and L55
//                .addPathParameter("music", "music")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null && response.get(
                                "data"
                            ).toString() != "null"
                        ) {
                            viewModelScope.launch {

                                try {
                                    parseJSONAndAddToDatabase(
                                        response.get("data") as JSONArray,
                                        context
                                    )
                                    Log.d("div", "TodosViewModel L726 ${response.get("data")}")
                                    _isRefreshing.value = false
                                    loadDailyTodoScore()

                                    autoScheduleDailyAlarms(context)  // set alarms for todos fetched
                                    Log.i("h_debug", "Todos Refreshed.")
                                } catch (e: Exception) {

                                }
                            }

                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d(
                            "ViewModel",
                            "API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )
                    }
                })

        }

    }

    suspend fun parseJSONAndAddToDatabase(todoListData: JSONArray, context: Context) {
        var i = 0
        coroutineScope {
            launch {
                val currentDate = DateClass().getCurrentDateAsString()

                while (i < todoListData.length()) {
                    val fetchedTodoData = todoListData.get(i) as JSONObject
                    val fetchedTodoId = fetchedTodoData.getInt("id")
                    val fetchedTodoType = fetchedTodoData.getString("type")

                    val checkStatus = todoDatabase.checkTodo(fetchedTodoId)

                    Log.d("checkStatus", "$checkStatus")
                    Log.d("typeTodo", "$fetchedTodoType")
                    when (fetchedTodoType) {
                        "daily" -> {
                            if (checkStatus == 1) {
                                refreshDailyTodo(
                                    fetchedTodoId,
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            } else if (checkStatus == 0) {
                                addNewDailyTodo(
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            }
                        }
                        "weekly" -> {
                            if (checkStatus == 1) {
                                refreshWeeklyTodo(
                                    fetchedTodoId,
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            } else if (checkStatus == 0) {
                                addNewWeeklyTodo(
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            }
                        }
                        "monthly" -> {
                            if (checkStatus == 1) {
                                refreshMonthlyTodo(
                                    fetchedTodoId,
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            } else if (checkStatus == 0) {
                                addNewMonthlyTodo(
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            }
                        }
                        "essentials" -> {
                            if (checkStatus == 1) {
                                refreshEssentialsTodo(
                                    fetchedTodoId,
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            } else if (checkStatus == 0) {
                                addNewEssentialsTodo(
                                    fetchedTodoData,
                                    currentDate,
                                    context
                                )
                            }
                        }
                    }
                    i++
                }
            }
        }
    }


    private fun refreshDailyTodo(
        fetchedTodoId: Int,
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {
            val todoToUpdate = todoDatabase.getTodo(fetchedTodoId)

            if (todoToUpdate != null) {

                if (fetchedTodoData.getString("alarmTime") != "null") {
                    todoToUpdate.alarmTimeByUser = fetchedTodoData.getString("alarmTime")
                }

                todoToUpdate.isCompleted = false
                todoToUpdate.task = fetchedTodoData.getString("name")
                todoToUpdate.alarmTime = fetchedTodoData.getString("defaultAlarmTime")

                todoToUpdate.isAlarmSet = false
                todoToUpdate.date = currentDate

                todoDatabase.update(todoToUpdate)
                UptoddSharedPreferences.getInstance(context)
                    .setLastDailyTodoFetchedDate(currentDate)
            }
        }
    }

    private fun refreshWeeklyTodo(
        fetchedTodoId: Int,
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {
            val todoToUpdate = todoDatabase.getTodo(fetchedTodoId)
            if (todoToUpdate != null) {
                val daysBeforeLastTodoRefresh =
                    DateClass().getDifferenceInDays(todoToUpdate.date, currentDate)
                if (daysBeforeLastTodoRefresh <= 7) return@launch

                if (fetchedTodoData.getString("alarmTime") != "null") {
                    todoToUpdate.alarmTimeByUser = fetchedTodoData.getString("alarmTime")
                }

                todoToUpdate.isCompleted = false
                todoToUpdate.task = fetchedTodoData.getString("name")
                todoToUpdate.alarmTime = fetchedTodoData.getString("defaultAlarmTime")


                todoToUpdate.isAlarmSet = false
                todoToUpdate.date = currentDate

                todoDatabase.update(todoToUpdate)
                UptoddSharedPreferences.getInstance(context)
                    .setLastWeeklyTodoFetchedDate(currentDate)
            }
        }
    }

    private fun refreshMonthlyTodo(
        fetchedTodoId: Int,
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {
            val todoToUpdate = todoDatabase.getTodo(fetchedTodoId)
            if (todoToUpdate != null) {
                val daysBeforeLastTodoRefresh =
                    DateClass().getDifferenceInDays(todoToUpdate.date, currentDate)
                if (daysBeforeLastTodoRefresh <= 30) return@launch

                if (fetchedTodoData.getString("alarmTime") != "null") {
                    todoToUpdate.alarmTimeByUser = fetchedTodoData.getString("alarmTime")
                }

                todoToUpdate.isCompleted = false
                todoToUpdate.task = fetchedTodoData.getString("name")
                todoToUpdate.alarmTime = fetchedTodoData.getString("defaultAlarmTime")

                todoToUpdate.isAlarmSet = false
                todoToUpdate.date = currentDate

                todoDatabase.update(todoToUpdate)
                UptoddSharedPreferences.getInstance(context)
                    .setLastMonthlyAndEssentialsTodoFetchedDate(currentDate)
            }
        }
    }

    private fun refreshEssentialsTodo(
        fetchedTodoId: Int,
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {
            val todoToUpdate = todoDatabase.getTodo(fetchedTodoId)
            if (todoToUpdate != null) {
                val daysBeforeLastTodoRefresh =
                    DateClass().getDifferenceInDays(todoToUpdate.date, currentDate)
                val age = KidsPeriod(context).getKidsAge()
                if (age == 0)
                    return@launch
                if (age <= 12 && daysBeforeLastTodoRefresh <= 90)
                    return@launch
                if (age > 12 && daysBeforeLastTodoRefresh <= 180)
                    return@launch

                if (fetchedTodoData.getString("alarmTime") != "null") {
                    todoToUpdate.alarmTimeByUser = fetchedTodoData.getString("alarmTime")
                }

                todoToUpdate.isCompleted = false
                todoToUpdate.task = fetchedTodoData.getString("name")
                todoToUpdate.alarmTime = fetchedTodoData.getString("defaultAlarmTime")

                todoToUpdate.isAlarmSet = false
                todoToUpdate.date = currentDate

                todoDatabase.update(todoToUpdate)
                UptoddSharedPreferences.getInstance(context)
                    .setLastMonthlyAndEssentialsTodoFetchedDate(currentDate)
            }
        }
    }

    private fun addNewDailyTodo(
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {

            val defaultAlarmTime = fetchedTodoData.getString("defaultAlarmTime")
            val alarmTimeByUser =
                if (fetchedTodoData.getString("alarmTime") == "null") defaultAlarmTime
                else fetchedTodoData.getString(
                    "alarmTime"
                )

            todoDatabase.insert(
                Todo(
                    id = fetchedTodoData.getInt("id"),
                    task = fetchedTodoData.getString("name"),
                    type = convertToTypeFromString(fetchedTodoData.getString("type")),
                    alarmTime = fetchedTodoData.getString("defaultAlarmTime"),
                    isCompleted = false,
                    isAlarmSet = false,
                    period = period,
                    alarmTimeInMilli = 0,
                    alarmTimeByUser = alarmTimeByUser,
                    alarmTimeByUserInMilli = 0,
                    isAlarmNeededByUser = defaultAlarmTime != "00:00:00",
                    weeklyMonday = false,
                    weeklyTuesday = false,
                    weeklyWednesday = false,
                    weeklyThursday = false,
                    weeklyFriday = false,
                    weeklySaturday = false,
                    weeklySunday = false,
                    date = currentDate,
                    doType = fetchedTodoData.getInt("doType"),
                    imageUrl = fetchedTodoData.getString("image")
                )
            )
            UptoddSharedPreferences.getInstance(context).setLastDailyTodoFetchedDate(currentDate)
        }
    }

    private fun addNewWeeklyTodo(
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {

            val defaultAlarmTime = fetchedTodoData.getString("defaultAlarmTime")
            val alarmTimeByUser =
                if (fetchedTodoData.getString("alarmTime") == "null") defaultAlarmTime
                else fetchedTodoData.getString(
                    "alarmTime"
                )

            var fetchedTodoDays = fetchedTodoData.getString("defaultAlarmDays")
            if (fetchedTodoDays == "") {
                fetchedTodoDays = "0,0,0,0,0,0,0"
            }

            todoDatabase.insert(
                Todo(
                    id = fetchedTodoData.getInt("id"),
                    task = fetchedTodoData.getString("name"),
                    type = convertToTypeFromString(fetchedTodoData.getString("type")),
                    alarmTime = fetchedTodoData.getString("defaultAlarmTime"),
                    isCompleted = false,
                    isAlarmSet = false,
                    period = period,
                    alarmTimeInMilli = 0,
                    alarmTimeByUser = alarmTimeByUser,
                    alarmTimeByUserInMilli = 0,
                    isAlarmNeededByUser = defaultAlarmTime != "00:00:00",
                    weeklyMonday = fetchedTodoDays[2] == '1',
                    weeklyTuesday = fetchedTodoDays[4] == '1',
                    weeklyWednesday = fetchedTodoDays[6] == '1',
                    weeklyThursday = fetchedTodoDays[8] == '1',
                    weeklyFriday = fetchedTodoDays[10] == '1',
                    weeklySaturday = fetchedTodoDays[12] == '1',
                    weeklySunday = fetchedTodoDays[0] == '1',
                    date = currentDate,
                    doType = fetchedTodoData.getInt("doType"),
                    imageUrl = fetchedTodoData.getString("image")

                )
            )

            UptoddSharedPreferences.getInstance(context).setLastWeeklyTodoFetchedDate(currentDate)

        }

    }

    private fun addNewMonthlyTodo(
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {

            val defaultAlarmTime = fetchedTodoData.getString("defaultAlarmTime")
            val alarmTimeByUser =
                if (fetchedTodoData.getString("alarmTime") == "null") defaultAlarmTime
                else fetchedTodoData.getString(
                    "alarmTime"
                )

            todoDatabase.insert(
                Todo(
                    id = fetchedTodoData.getInt("id"),
                    task = fetchedTodoData.getString("name"),
                    type = convertToTypeFromString(fetchedTodoData.getString("type")),
                    alarmTime = fetchedTodoData.getString("defaultAlarmTime"),
                    isCompleted = false,
                    isAlarmSet = false,
                    period = period,
                    alarmTimeInMilli = 0,
                    alarmTimeByUser = alarmTimeByUser,
                    alarmTimeByUserInMilli = 0,
                    isAlarmNeededByUser = defaultAlarmTime != "00:00:00",
                    weeklyMonday = false,
                    weeklyTuesday = false,
                    weeklyWednesday = false,
                    weeklyThursday = false,
                    weeklyFriday = false,
                    weeklySaturday = false,
                    weeklySunday = false,
                    date = currentDate,
                    doType = fetchedTodoData.getInt("doType"),
                    imageUrl = fetchedTodoData.getString("image")
                )
            )
            Log.d("insert daily", "daily")
            UptoddSharedPreferences.getInstance(context)
                .setLastMonthlyAndEssentialsTodoFetchedDate(currentDate)

        }
    }

    private fun addNewEssentialsTodo(
        fetchedTodoData: JSONObject,
        currentDate: String,
        context: Context
    ) {
        viewModelScope.launch {

            val defaultAlarmTime = fetchedTodoData.getString("defaultAlarmTime")
            val alarmTimeByUser =
                if (fetchedTodoData.getString("alarmTime") == "null") defaultAlarmTime
                else fetchedTodoData.getString(
                    "alarmTime"
                )

            todoDatabase.insert(
                Todo(
                    id = fetchedTodoData.getInt("id"),
                    task = fetchedTodoData.getString("name"),
                    type = convertToTypeFromString(fetchedTodoData.getString("type")),
                    alarmTime = fetchedTodoData.getString("defaultAlarmTime"),
                    isCompleted = false,
                    isAlarmSet = false,
                    period = period,
                    alarmTimeInMilli = 0,
                    alarmTimeByUser = alarmTimeByUser,
                    alarmTimeByUserInMilli = 0,
                    isAlarmNeededByUser = defaultAlarmTime != "00:00:00",
                    weeklyMonday = false,
                    weeklyTuesday = false,
                    weeklyWednesday = false,
                    weeklyThursday = false,
                    weeklyFriday = false,
                    weeklySaturday = false,
                    weeklySunday = false,
                    date = currentDate,
                    doType = fetchedTodoData.getInt("doType"),
                    imageUrl = fetchedTodoData.getString("image")

                )
            )

            UptoddSharedPreferences.getInstance(context)
                .setLastMonthlyAndEssentialsTodoFetchedDate(currentDate)

        }
    }

    private fun updateAlarmThroughApiUsingWorkManager(todoId: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = Data.Builder()
        data.putInt("todoToUpdate", todoId)


        val alarmUpdaterWorker =
            OneTimeWorkRequestBuilder<UpdateAlarmThroughApiWorker>()
                .setConstraints(constraints)
                .setInputData(data.build())
                .setInitialDelay(10, TimeUnit.MINUTES)
                .build()

        val workManager = WorkManager.getInstance(getApplication())
        workManager.enqueue(alarmUpdaterWorker)
    }

    private fun convertToTypeFromString(type: String): Int {
        return when (type) {
            "daily" -> DAILY_TODO
            "monthly" -> MONTHLY_TODO
            "weekly" -> WEEKLY_TODO
            "essentials" -> ESSENTIALS_TODO
            else -> 0
        }
    }

    fun updateIdealWeightAndHeight(activity: Activity) {

        val preferences: SharedPreferences =
            activity.getSharedPreferences("IDEAL_SIZE", Context.MODE_PRIVATE)

        val editor = preferences.edit()
        val month = KidsPeriod(activity).getKidsAge()

        if (!preferences.contains("idealWeight") || !preferences.contains("idealHeight")) {
            var stage = UptoddSharedPreferences.getInstance(activity).getStage()
            viewModelScope.launch {
                AndroidNetworking.get("https://www.uptodd.com/api/idealsize/${if (stage == "prenatal" || stage == "pre birth") -1 else month}")        //replace music by blog in L54 and L55
                    .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null) {
                                val data = response.get("data") as JSONObject
                                val weight = data.getString("weight")
                                val height = data.getString("height").trim()

                                idealHeight.value = "$height cm"
                                idealWeight.value = "$weight  Kg"

                                editor.putString("idealWeight", weight)
                                editor.putString("idealHeight", height)
                                editor.commit()
                            }
                        }

                        override fun onError(anError: ANError?) {
                            Log.d(
                                "ViewModel",
                                "API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                            )
                        }
                    })
            }
        } else {
            val weight = preferences.getString("idealWeight", "")
            val height = preferences.getString("idealHeight", "")
            idealHeight.value = "$height cm"
            idealWeight.value = "$weight  Kg"
        }

    }

    override fun onCleared() {
        super.onCleared()

        viewModelScope.cancel()
        //viewModelJob.cancel()
    }

    fun startMusicDownload(
        destinationDir: File,
        uptoddDownloadManager: DownloadManager,
        context: Context
    ) {
        viewModelScope.launch {


            val stage = UptoddSharedPreferences.getInstance(context).getStage()
            downloadedMusic = musicDatabase.getAllFiles()
            downloadedMemoryMusic = memoryDatabase.getAllFiles()
            Log.i("downloaded", downloadedMusic.size.toString())
            val language = AllUtil.getLanguage()
            val userType = UptoddSharedPreferences.getInstance(context).getUserType()
            val country = AllUtil.getCountry(context)
            AndroidNetworking.get("https://www.uptodd.com/api/musics?lang=$language&userType=$userType&country=$country&motherStage=$stage")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {

                            UptoddSharedPreferences.getInstance(context).saveMDownStatus(true)
                            viewModelScope.launch {
                                try {
                                    val apiFiles =
                                        AllUtil.getAllMusic(response.get("data").toString())
                                    val destDir = File(destinationDir, "music")
                                    downloadMusicFiles(
                                        apiFiles,
                                        destDir,
                                        uptoddDownloadManager,
                                        context
                                    )
                                } catch (e: Exception) {

                                }
                            }
                        }
                    }

                    override fun onError(error: ANError) {
                    }
                })


            AndroidNetworking.get("https://www.uptodd.com/api/poems?userType=$userType&country=$country&motherStage=$stage")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {
                            viewModelScope.launch {
                                try {
                                    val poems = AllUtil.getAllMusic(response.get("data").toString())
                                    val destDir = File(destinationDir, "poem")
                                    downloadPoemFiles(poems, destDir, uptoddDownloadManager)
                                } catch (e: java.lang.Exception) {

                                }
                            }
                        }
                    }

                    override fun onError(error: ANError) {}
                })

            val uid = AllUtil.getUserId()
            val prenatal = if (stage == "pre birth" || stage == "prenatal") 0 else 1
            val lang = AllUtil.getLanguage()
            AndroidNetworking.get("https://www.uptodd.com/api/memorybooster?userId={userId}&prenatal={prenatal}&lang={lang}&userType=$userType&country=$country&motherStage=$stage")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .addPathParameter("userId", uid.toString())
                .addPathParameter("prenatal", prenatal.toString())
                .addPathParameter("lang", lang)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {
                            viewModelScope.launch {
                                try {
                                    val speedBooster =
                                        AllUtil.getAllMemoryFiles(response.get("data").toString())
                                    val destDir = File(destinationDir, "speedbooster")
                                    downloadSpeedBoosterFiles(
                                        speedBooster,
                                        destDir,
                                        uptoddDownloadManager
                                    )
                                } catch (e: java.lang.Exception) {

                                }
                            }
                        } else {
                            apiError = response.getString("message")
                            _isLoading.value = -1
                        }
                    }

                    override fun onError(error: ANError) {
                        apiError = error.message.toString()
                        _isLoading.value = -1
                        Log.i("error", error.errorBody)
                    }
                })


        }

    }

    fun getNPDetails(context: Context) {
        val uid = AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/nonPremiumAppusers/initialSetupDetails/${uid}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {
                        viewModelScope.launch {

                            if (response["data"].toString() == "null") {
                                _isNewUser.value = true
                            } else {
                                Log.d("data", response["data"].toString())
                                val nonPremiumAccount =
                                    AllUtil.getNonPAccount(response.get("data").toString())
                                UptoddSharedPreferences.getInstance(context)
                                    .saveNonPAccount(nonPremiumAccount)
                                nonPremiumAccount.anythingSpecial?.let {
                                    Log.d(
                                        "NPDetails",
                                        response.getString("data").toString()
                                    )
                                }
                                _isNewUser.value = false
                            }
                        }

                    } else {
                        apiError = response.getString("message")
                        _isLoading.value = -1
                        _isNewUser.value = false
                    }
                }

                override fun onError(error: ANError) {
                    apiError = error.message.toString()
                    _isLoading.value = -1
                    Log.e("errorNonpremim", error.errorBody)
                }
            })
    }

    fun downloadPoemFiles(
        poems: List<MusicFiles>,
        destinationDir: File,
        mManager: DownloadManager
    ) {
        poems.forEach { poem ->
            if (!getIsMusicDownloaded(poem)) {
                _showDownloadingFlag.value = true
                val file = File(destinationDir.path, "${poem.file}.aac")
                if (file.exists()) {
                    updatePath(poem, file.path)
                    return@downloadPoemFiles
                }
                if (!file.exists())
                    destinationDir.mkdirs()
                if (!destinationDir.canWrite())
                    destinationDir.setWritable(true)

//                val destinationUri = Uri.fromFile(file)

                Log.i(
                    "inserting",
                    "starting -> ${poem.name}. Downloading from: https://www.uptodd.com/files/poem/${poem.file!!.trim()}.aac"
                )

                val request: DownloadRequest = DownloadRequest.Builder()
                    .url("https://www.uptodd.com/files/poem/${poem.file!!.trim()}.aac")
                    .retryTime(3)
                    .retryInterval(2, TimeUnit.SECONDS)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .priority(com.coolerfall.download.Priority.HIGH)
                    //.allowedNetworkTypes(DownloadRequest.NETWORK_WIFI)
                    .destinationFilePath(file.path)
                    .downloadCallback(object : DownloadCallback {
                        override fun onStart(downloadId: Int, totalBytes: Long) {
                            Log.i(
                                "inserting",
                                "on start -? ${poem.name}"
                            )
                        }

                        override fun onRetry(downloadId: Int) {}
                        override fun onProgress(
                            downloadId: Int,
                            bytesWritten: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(downloadId: Int, filePath: String) {
                            updatePath(poem, file.path)
                            Log.i("inserting", "on success")
                        }

                        override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                            Log.i(
                                "inserting",
                                "on failed -> ${poem.name}. Cause: $errMsg"
                            )
                        }
                    })
                    .build()

                val downloadId: Int = mManager.add(request)

//                uptoddDownloadManager.initializeDownloadManager("https://uptodd.com/files/poem/${poem.name!!.trim()}.aac", "${poem.file}.aac")
//                uptoddDownloadManager.setDownloadListener(object: JishnuDownloadManager.DownloadListener{
//                    override fun onCancel() {
//                        Log.i("inserting", "on cancelled")
//
//                    }
//
//                    override fun onSuccess() {
//                        updatePath(poem, file.path)
//                        Log.i("inserting", "on success")
//
//                    }
//
//                    override fun onFailed(throwable: Throwable) {
//                        Log.i("inserting", "on failed")
//                    }
//
//                })
//                uptoddDownloadManager.downloadFile(destinationUri)

//                uptoddDownloadManager.setDestinationUri(destinationUri)
//                uptoddDownloadManager.setUrl("https://uptodd.com/files/poem/${poem.name!!.trim()}.aac")
//                uptoddDownloadManager.setListener(object : UpToddDownloadManager.DownloadListener {
//                    override fun onProgress(progress: Float) {
//                        if(progress>0.95f)
//                            updatePath(poem, file.path)
//                    }
//
//                    override fun onSuccess(path: String) {
//                    }
//
//                    override fun onFailed(throwable: Throwable) {
//
//                    }
//                })
//                uptoddDownloadManager.download()
            }
        }
    }


    private fun downloadMusicFiles(
        files: List<MusicFiles>,
        destinationDir: File,
        mManager: DownloadManager, context: Context
    ) {
        files.forEach {
            if (!getIsMusicDownloaded(it)) {
                _showDownloadingFlag.value = true
                val file = File(destinationDir.path, "${it.file}.aac")
                if (file.exists()) {
                    updatePath(it, file.path)
                    return@downloadMusicFiles
                }
                if (!file.exists())
                    destinationDir.mkdirs()
                if (!destinationDir.canWrite())
                    destinationDir.setWritable(true)

                val destinationUri = Uri.fromFile(file)
                Log.i(
                    "inserting",
                    "starting -> ${it.name}. Downloading from : https://www.uptodd.com/files/music/${it.image!!.trim()}/${it.file!!.trim()}.aac"
                )

                val request: DownloadRequest = DownloadRequest.Builder()
                    .url("https://www.uptodd.com/files/music/${it.image!!.trim()}/${it.file!!.trim()}.aac")
                    .retryTime(3)
                    .retryInterval(2, TimeUnit.SECONDS)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .priority(com.coolerfall.download.Priority.HIGH)
                    //.allowedNetworkTypes(DownloadRequest.NETWORK_WIFI)
                    .destinationFilePath(file.path)
                    .downloadCallback(object : DownloadCallback {
                        override fun onStart(downloadId: Int, totalBytes: Long) {
                            Log.i(
                                "inserting",
                                "on start -> ${it.name}"
                            )
                        }

                        override fun onRetry(downloadId: Int) {}
                        override fun onProgress(
                            downloadId: Int,
                            bytesWritten: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(downloadId: Int, filePath: String) {
                            if (files.last().id == it.id) {
                                UptoddSharedPreferences.getInstance(context).saveMDownStatus(false)
                            }
                            updatePath(it, file.path)
                            Log.i("inserting", "on success")
                        }

                        override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                            Log.i(
                                "inserting",
                                "on failed -> ${it.name}. Cause: $errMsg"
                            )
                        }
                    })
                    .build()

                val downloadId: Int = mManager.add(request)


//                uptoddDownloadManager.initializeDownloadManager("https://uptodd.com/files/music/${it.image!!.trim()}/${it.file!!.trim()}.aac", "${it.file}.aac")
//                uptoddDownloadManager.setDownloadListener(object: JishnuDownloadManager.DownloadListener{
//                    override fun onCancel() {
//                        Log.i("inserting", "on cancelled")
//                    }
//
//                    override fun onSuccess() {
//                        updatePath(it, file.path)
//                        Log.i("inserting", "on success")
//                    }
//
//                    override fun onFailed(throwable: Throwable) {
//                        Log.i("inserting", "on failed")
//                    }
//
//                })
//                uptoddDownloadManager.downloadFile(destinationUri)

//                uptoddDownloadManager.setDestinationUri(destinationUri)
//                uptoddDownloadManager.setUrl("https://uptodd.com/files/music/${it.image!!.trim()}/${it.file!!.trim()}.aac")
//                uptoddDownloadManager.setListener(object : UpToddDownloadManager.DownloadListener {
//                    override fun onProgress(progress: Float) {
//                        if(progress<1F){
//                            Log.i("progress", (progress*100).toString())
//                        }
//                        if(progress>0.95f)
//                            updatePath(it, file.path)
//                    }
//
//                    override fun onSuccess(path: String) {}
//
//                    override fun onFailed(throwable: Throwable) {
//
//                    }
//                })
//                uptoddDownloadManager.download()
            }
        }
    }

    fun downloadSpeedBoosterFiles(
        files: List<MemoryBoosterFiles>,
        destinationDir: File,
        mManager: DownloadManager
    ) {
        files.forEach {
            if (!getIsMemoryDownloded(it)) {
                _showDownloadingFlag.value = true
                val file = File(destinationDir.path, "${it.file}.aac")
                if (file.exists()) {
                    updateMemoryPath(it, file.path)
                    return@downloadSpeedBoosterFiles
                }
                if (!file.exists())
                    destinationDir.mkdirs()
                if (!destinationDir.canWrite())
                    destinationDir.setWritable(true)

                val destinationUri = Uri.fromFile(file)
                Log.i(
                    "inserting",
                    "starting -> ${it.name}. Downloading from : https://www.uptodd.com/files/memory_booster/${it.file!!.trim()}.aac"
                )


                val request: DownloadRequest = DownloadRequest.Builder()
                    .url("https://www.uptodd.com/files/memory_booster/${it.file!!.trim()}.aac")
                    .retryTime(3)
                    .retryInterval(2, TimeUnit.SECONDS)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .priority(com.coolerfall.download.Priority.HIGH)
                    //.allowedNetworkTypes(DownloadRequest.NETWORK_WIFI)
                    .destinationFilePath(file.path)
                    .downloadCallback(object : DownloadCallback {
                        override fun onStart(downloadId: Int, totalBytes: Long) {
                            Log.i(
                                "inserting",
                                "on start -> ${it.name}"
                            )
                        }

                        override fun onRetry(downloadId: Int) {}
                        override fun onProgress(
                            downloadId: Int,
                            bytesWritten: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(downloadId: Int, filePath: String) {
                            updateMemoryPath(it, file.path)
                            Log.i("inserting", "on success")
                        }

                        override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                            Log.i(
                                "inserting",
                                "on failed -> ${it.name}. Cause: $errMsg"
                            )
                        }
                    })
                    .build()

                val downloadId: Int = mManager.add(request)
            }
        }
    }

    fun downloadGuidelines(context: Context) {
        val downloader = Downloader
        downloader.startDocumentDownload(context,
            "https://www.uptodd.com/resources/user/UserGuide.pdf",
            "UptoddAppGuidelines",
            object : DownloadCallback {
                override fun onStart(downloadId: Int, totalBytes: Long) {
                    Log.d("download", "started")
                }

                override fun onRetry(downloadId: Int) {}
                override fun onProgress(
                    downloadId: Int,
                    bytesWritten: Long,
                    totalBytes: Long
                ) {
                }

                override fun onSuccess(downloadId: Int, filePath: String) {
                    //
                }

                override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                    //
                }
            })
    }

    private fun isMusic(song: MusicFiles): Boolean {
        return song.language == null
    }

    private fun updatePath(music: MusicFiles, path: String) {
        Log.i("inserting", "inserting init")
        viewModelScope.launch {
            music.filePath = path
            if (isMusic(music))
                music.language = "NA"
            Log.i("inserting", "${music.name} -> ${music.filePath}")
            musicDatabase.insert(music)
        }
    }

    private fun updateMemoryPath(music: MemoryBoosterFiles, path: String) {
        Log.i("inserting", "inserting init")
        viewModelScope.launch {
            music.filePath = path
            Log.i("inserting", "${music.name} -> ${music.filePath}")
            memoryDatabase.insert(music)
        }
    }

    private fun getIsMusicDownloaded(music: MusicFiles): Boolean {
        downloadedMusic.forEach {
            if (it.id == music.id)
                return@getIsMusicDownloaded true
        }
        return false
    }


    private fun getIsMemoryDownloded(m: MemoryBoosterFiles): Boolean {
        downloadedMemoryMusic.forEach {
            if (it.id == m.id)
                return@getIsMemoryDownloded true
        }
        return false
    }

    fun getWebinars() {
        //TODO change this to webinars when they are ready
        _isLoading.value = 1
        AndroidNetworking.get("https://www.uptodd.com/api/blogs?page=0")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        parseJSONWebinars(response.get("data") as JSONArray)
                    }
                    _isLoading.value = 0
                }

                override fun onError(anError: ANError?) {
                    apiError = anError!!.message!!
                    AllUtil.logApiError(anError)
                    _isLoading.value = -1
                }

            })
    }

    private fun parseJSONWebinars(jsonArray: JSONArray) {
        Log.d("div", "Size ${jsonArray.length()}")
        val list = ArrayList<Webinars>()
        var i = 0
        //TODO change this to webinars when they are ready
        val appendable = "https://www.uptodd.com/images/app/android/thumbnails/blogs/$dpi/"
        while (i < 4) {
            if (jsonArray.length() == i)
                break
            val obj = jsonArray.get(i) as JSONObject
//            Log.d("div","BlogsListViewModel L116 $obj")
            list.add(
                Webinars(
                    webinarId = obj.getLong("id"),
                    imageURL = appendable + obj.getString("thumbnail") + ".webp",
                    webinarURL = "https://uptodd.com/" + obj.getString("blogUrl"),
                    title = obj.getString("title")
                )
            )
            i++
        }
        _webinars.value = list
    }


}