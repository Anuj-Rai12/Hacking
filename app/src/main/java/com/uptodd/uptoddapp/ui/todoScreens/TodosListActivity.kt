package com.uptodd.uptoddapp.ui.todoScreens

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.OkHttpDownloader
import com.google.android.material.navigation.NavigationView
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddViewModelFactory
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.TodosViewModel
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus.Companion.context
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.DEFAULT_HOMEPAGE_INTENT
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import com.uptodd.uptoddapp.workManager.*
import com.uptodd.uptoddapp.workManager.alarmSchedulerWorkmanager.DailyAlarmSchedulerWorker
import com.uptodd.uptoddapp.workManager.alarmSchedulerWorkmanager.MonthlyAndEssentialsAlarmSchedulerWorker
import com.uptodd.uptoddapp.workManager.alarmSchedulerWorkmanager.WeeklyAlarmSchedulerWorker
import com.uptodd.uptoddapp.workManager.updateApiWorkmanager.UpgradeWorkManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


class TodosListActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var cameraPermissinGranted: Boolean = false
    private var storagePermissinGranted: Boolean = false

    private val STORAGE_PERMISSION_REQUEST_CODE = 0
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var uiScope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()

        var userType: String? = ""

        preferences = getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)


        if (preferences.contains("userType"))
            userType = preferences.getString("userType", "")

        if (userType == "Nanny")
            inflateNannyMode()
        else if (userType == "Normal")
            inflateNormalMode()

        hasStoragePermission()

        requestFireAllWorkManagers()

        if(intent.getIntExtra("showUp",0)==1)
        {
            findNavController(R.id.home_page_fragment).navigate(R.id.action_homePageFragment_to_upgradeFragment)
        }
        else
        { if(!AllUtil.isUserPremium(this)) {

            val upToddDialogs = UpToddDialogs(this)
            upToddDialogs.showInfoDialog("Thank you -Welcome to UpTodd,we'll help you in this rapid program to boost overall baby's development","Next",
                object :UpToddDialogs.UpToddDialogListener {
                    override fun onDialogButtonClicked(dialog: Dialog) {
                        dialog.dismiss()
                    }

                    override fun onDialogDismiss() {
                        upToddDialogs.showInfoDialog("Note-This is rapid program ,so features are limited","Upgrade Now",
                            object :UpToddDialogs.UpToddDialogListener
                            {
                                override fun onDialogButtonClicked(dialog: Dialog) {

                                    try {
                                        findNavController(R.id.home_page_fragment).navigate(R.id.action_homePageFragment_to_upgradeFragment)
                                    }catch (e:Exception)
                                    {
                                        Log.e("dialog error",e.localizedMessage)
                                    }
                                    dialog.dismiss()
                                }

                            }
                        )
                    }

                }
            )
        }
        }

        val viewModelFactory = UptoddViewModelFactory.getInstance(application)

        val viewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(TodosViewModel::class.java)


        viewModel.notificationIntent.value = intent.getIntExtra(
            "notificationIntent",
            DEFAULT_HOMEPAGE_INTENT
        )
        viewModel.notificationIntentExtras.value = intent.extras



        val manager: DownloadManager = DownloadManager.Builder().context(this)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(3)
            .logger { message -> Log.d("TAG", message!!) }
            .build()

        viewModel.startMusicDownload(
            File(
                getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "Downloads"
            ),
            manager,this
        )


        if(!AllUtil.isUserPremium(this)) {
            viewModel.getNPDetails(this)
            viewModel.isNewUser.observe(this, Observer {

                if(it)
                {
                }
                else
                {
                    val np = UptoddSharedPreferences.getInstance(this).getNonPAccount()
                    np.kidsDob?.let { Log.d("kidsDob", it) }
                    np.motherStage?.let { Log.d("stage", it) }
                    np.anythingSpecial?.let { Log.d("anythingSpecial", it) }
                    np.expectedMonthsOfDelivery?.let { Log.d("expectedMonthOfDelivery", it) }
                }
            })
        }
    }


    private fun inflateNormalMode() {

        setContentView(R.layout.activity_todos_list)

        drawerLayout = findViewById(R.id.main_drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view_main)

        val headerView = navView.getHeaderView(0)
        val headerImage: CircleImageView = headerView.findViewById(R.id.profile_image)
        val textViewHeader: TextView = headerView.findViewById(R.id.textView45)
        var name: String? = "Baby"
        if (preferences!!.contains("babyName"))
            name = preferences!!.getString("babyName", "Baby")
        if (name == null || name == "baby")
            name = "Baby"
        textViewHeader.text = name

        initialiseBabyPhoto(headerImage)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homePageFragment,
                R.id.music,
                R.id.poemFragment,
                R.id.toysFragment,
                R.id.coloursFragment,
                R.id.storiesFragment,
                R.id.allYogaFragment,
                R.id.allTodosViewPagerFragment,
                R.id.editAlarmsViewPagerFragment,
                R.id.expectedOutcomesFragment3,
                R.id.dietFragment,
                R.id.vaccinationFragment,
                R.id.blogsFragment,
                R.id.activitySampleFragment,
                R.id.activityPodcastFragment,
                R.id.captureImageFragment,
                R.id.orderListFragment,
                R.id.referFragment,
                R.id.referListFragment,
                R.id.allTicketsFragment,
                R.id.accountFragment2
            ), drawerLayout
        )


        val country=if(UptoddSharedPreferences.getInstance(this).getPhone()?.startsWith("+91")!!)
            "india"
        else
            "row"
        if(country=="row")
        navView.menu.findItem(R.id.orderListFragment).title=("Expert prescription")

        navController = findNavController(R.id.home_page_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }


    private fun inflateNannyMode() {

        setContentView(R.layout.nanny_todos_list)

        drawerLayout = findViewById(R.id.nanny_drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view_main_nanny)

        val headerView = navView.getHeaderView(0)
        val headerImage: CircleImageView = headerView.findViewById(R.id.profile_image)
        initialiseBabyPhoto(headerImage)
        val textViewHeader: TextView = headerView.findViewById(R.id.textView45)
        var name: String? = "Baby"
        if (preferences.contains("babyName"))
            name = preferences.getString("babyName", "Baby")
        if (name == null || name == "baby")
            name = "Baby"
        textViewHeader.text = name

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homePageFragment,
                R.id.music,
                R.id.poemFragment,
                R.id.toysFragment,
                R.id.storiesFragment,
                R.id.dietFragment,
                R.id.speedBoosterFragment,
                R.id.changeLanguageNanny
            ), drawerLayout
        )

        navController = findNavController(R.id.home_page_fragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }


    private fun initialiseBabyPhoto(headerImage: CircleImageView) {
        try {
            var preferences: SharedPreferences?
            preferences = getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
            var url = ""
            if (preferences!!.contains("profileImageUrl"))
                url = preferences.getString("profileImageUrl", "").toString()
            var stage = "pre"
            if (preferences.contains("babyName") && preferences.getString("babyName", "") != "null"
                && preferences.getString("babyName", "") != "baby"
            )
                stage = "post"

            var res: Int
            if (stage == "pre")
                res = R.drawable.pre_birth_profile
            else if (stage == "post")
                res = R.drawable.post_birth_profile
            else
                res = R.drawable.default_set_android_thumbnail

            if (url == "null" || url == "") {
                headerImage.setImageResource(res)
            } else {
                url = "https:uptodd.com/uploads/$url"


                var imageFile: File?
                val folder =
                    File(
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            .toString() + "/UpTodd"
                    )
                var success = true
                if (!folder.exists()) {
                    success = folder.mkdirs()
                }
                if (success) {
                    imageFile = File(folder.absolutePath + File.separator + "Profile.jpg")

                    if (!imageFile.exists()) {
                        Log.d("div", "HomePageFragment L205")
                        Glide.with(headerImage.context)
                            .load(url)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(res)
                            )
                            .into(headerImage)

                        imageFile.createNewFile()

                        Glide.with(this).asBitmap().load(url)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    headerImage.setImageBitmap(resource)

                                    val ostream = ByteArrayOutputStream()
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, ostream)
                                    val fout = FileOutputStream(imageFile)
                                    fout.write(ostream.toByteArray())
                                    fout.close()
                                    val values = ContentValues()
                                    values.put(
                                        MediaStore.Images.Media.DATE_TAKEN,
                                        System.currentTimeMillis()
                                    )
                                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                    values.put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
                                    contentResolver?.insert(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        values
                                    )
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Log.d("div", "GenerateCardFragment L190 $placeholder")
                                }
                            })
                    } else {
                        headerImage.setImageBitmap(BitmapFactory.decodeFile(imageFile.absolutePath))
                    }


                } else {
                    Toast.makeText(this, getString(R.string.image_not_saved), Toast.LENGTH_SHORT)
                        .show()
                    return
                }


            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }


    }

    //storage permission
    private fun hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            } else {
                storagePermissinGranted = true
            }
        } else {
            storagePermissinGranted = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
            if (requestCode == CAMERA_PERMISSION_REQUEST_CODE)
                cameraPermissinGranted = true
            if (requestCode == STORAGE_PERMISSION_REQUEST_CODE)
                storagePermissinGranted = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun requestFireAllWorkManagers() {

        val uptoddSharedPreferences = UptoddSharedPreferences.getInstance(this)

        uiScope.launch {
            val isWorkManagerFired = uptoddSharedPreferences.getWorkManagerFiredStatus()

            if (!isWorkManagerFired) {
                fireDailyAlarmWorkManager()
                fireWeeklyAlarmWorkManager()
                fireMonthlyAndEssentialsAlarmWorkManager()
                fireDailyCheckWorkManager()
                fireDailySubscriptionCheckWorkManager()
                fireUpgradeCheckWorkManager()
                uptoddSharedPreferences.setWorkManagerFiredStatusTrue()
            } else Log.d("Todos list Activity", "Work manager fired already")
        }
    }

    private fun fireDailyAlarmWorkManager() {
        val dailyAlarmScheduler =
            PeriodicWorkRequestBuilder<DailyAlarmSchedulerWorker>(5, TimeUnit.HOURS)
                .addTag(DAILY_WORK_MANAGER_TAG)
                .build()

        val workManager = WorkManager.getInstance(application)
        workManager.enqueue(dailyAlarmScheduler)
    }

    private fun fireWeeklyAlarmWorkManager() {
        val weeklyAlarmScheduler =
            PeriodicWorkRequestBuilder<WeeklyAlarmSchedulerWorker>(6, TimeUnit.HOURS)
                .addTag(WEEKLY_WORK_MANAGER_TAG)
                .build()

        val workManager = WorkManager.getInstance(application)
        workManager.enqueue(weeklyAlarmScheduler)

        Log.d("Work manager", "Main Activity - WM fired for weekly alarm")      // log
    }

    private fun fireMonthlyAndEssentialsAlarmWorkManager() {
        val monthlyAndEssentialsAlarmScheduler =
            PeriodicWorkRequestBuilder<MonthlyAndEssentialsAlarmSchedulerWorker>(15, TimeUnit.HOURS)
                .addTag(MONTHLY_AND_ESSENTIALS_WORK_MANAGER_TAG)
                .build()

        val workManager = WorkManager.getInstance(application)
        workManager.enqueue(monthlyAndEssentialsAlarmScheduler)

        Log.d(
            "Work manager",
            "Main Activity - WM fired for monthly and essentials alarm"
        )      // log
    }

    private fun fireDailyCheckWorkManager() {
        val dailyCheckWorker = PeriodicWorkRequestBuilder<DailyCheck>(6, TimeUnit.HOURS)
            .addTag(DAILYCHECK_WORK_MANAGER_TAG)
            .build()


        val workManager = WorkManager.getInstance(application)
        workManager.enqueue(dailyCheckWorker)
    }
    private fun fireUpgradeCheckWorkManager() {
        val dailyCheckWorker = OneTimeWorkRequestBuilder<UpgradeWorkManager>()
            .addTag(DAILY_UP_CHECK_WORK_MANAGER)
            .build()


        val workManager = WorkManager.getInstance(application)
        workManager.enqueue(dailyCheckWorker)
        Log.d(
            "Work manager",
            "Upgrade Work manager triggered"
        )
    }

    private fun fireDailySubscriptionCheckWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val dailySubscriptionCheckWorker =
            PeriodicWorkRequestBuilder<DailySubscriptionCheck>(15, TimeUnit.MINUTES)
                .addTag(DAILY_SUBSCRIPTION_CHECK_WORK_MANAGER_TAG)
                .setConstraints(constraints)
                .build()

        val workManager = WorkManager.getInstance(application)
        workManager.enqueue(dailySubscriptionCheckWorker)
    }
}