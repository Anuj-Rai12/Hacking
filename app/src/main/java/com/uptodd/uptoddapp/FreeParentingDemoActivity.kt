package com.uptodd.uptoddapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.uptodd.uptoddapp.databinding.ActvityFreeDemoBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import com.uptodd.uptoddapp.utils.getEmojiByUnicode
import com.uptodd.uptoddapp.utils.setLogCat
import com.uptodd.uptoddapp.workManager.DAILY_SUBSCRIPTION_CHECK_WORK_MANAGER_TAG
import com.uptodd.uptoddapp.workManager.DailySubscriptionCheck
import com.uptodd.uptoddapp.workManager.FREE_PARENTING_PROGRAM
import com.uptodd.uptoddapp.workManager.FreeParentingWorkManger
import java.util.concurrent.TimeUnit

class FreeParentingDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActvityFreeDemoBinding
    private lateinit var navController: NavController

    private val viewModel: LoginViewModel by viewModels()

    private val showHomeDashBoard by lazy {
        intent.getBooleanExtra("showFreeParenting", false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.actvity_free_demo)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        val inflater = navHostFragment.navController.navInflater

        val graph = inflater.inflate(R.navigation.free_parenting_navgraph)

        if (showHomeDashBoard) {
            viewModel.getRequestLoginRequest?.let { res ->
                LoginSingletonResponse.getInstance().setLoginRequest(res)
                graph.startDestination = R.id.freeDemoBashBoardFragment
            } ?: run {
                showDialogBox(
                    "Failed",
                    "Cannot login into account ${getEmojiByUnicode(0x1F615)}\n Try to Login Again!!",
                    icon = android.R.drawable.stat_notify_error
                ) {}
                graph.startDestination = R.id.parentingLoginFragment
            }
        } else {
            graph.startDestination = R.id.parentingLoginFragment
        }
        navController = navHostFragment.findNavController()
        navController.setGraph(graph, intent.extras)

        uploadWorkManger()

    }

    private fun uploadWorkManger() {


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val dailySubscriptionCheckWorker =
            PeriodicWorkRequestBuilder<DailySubscriptionCheck>(15, TimeUnit.MINUTES)
                .addTag(DAILY_SUBSCRIPTION_CHECK_WORK_MANAGER_TAG)
                .setConstraints(constraints)
                .build()

        val workManager = WorkManager.getInstance(application)

        val freeParentingWork =
            PeriodicWorkRequestBuilder<FreeParentingWorkManger>(15, TimeUnit.MINUTES)
                .addTag(FREE_PARENTING_PROGRAM)
                .setConstraints(constraints)
                .build()

        workManager.enqueue(freeParentingWork)
        workManager.getWorkInfoByIdLiveData(freeParentingWork.id).observe(this) {
            setLogCat("WORK_FREE","${it.state}")
        }
    }


    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }

}