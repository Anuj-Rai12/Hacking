package com.uptodd.uptoddapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.uptodd.uptoddapp.databinding.ActvityFreeDemoBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.ui.loginfreeorpaid.LoginSelectionOption
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox

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

        viewModel.event.observe(this) {
            it.getContentIfNotHandled()?.let { err ->
                showErrorDialog(err, true)
            }
        }
        getLoginResponse()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        val inflater = navHostFragment.navController.navInflater

        val graph = inflater.inflate(R.navigation.free_parenting_navgraph)

        if (showHomeDashBoard) {
            viewModel.getRequestLoginRequest?.let { res ->
                LoginSingletonResponse.getInstance().setLoginRequest(res)
                viewModel.fetchResponse(res)
                graph.startDestination = R.id.DailyBookFragment
            } ?: run {
                showErrorDialog(
                    "Cannot login into account ${getEmojiByUnicode(0x1F615)}\n Try to Login Again!!",
                    true
                )
                graph.startDestination = R.id.parentingLoginFragment
            }
        } else {
            graph.startDestination = R.id.parentingLoginFragment
        }
        navController = navHostFragment.findNavController()
        navController.setGraph(graph, intent.extras)
        binding.bottomNavBar.setupWithNavController(navController)
    }

    private fun showErrorDialog(msg: String, isCancel: Boolean) {
        showDialogBox(
            "Failed",
            msg,
            icon = android.R.drawable.stat_notify_error,
            isCancel = isCancel
        ) {}
    }


    fun hideBottomNavBar() {
        binding.bottomNavBar.hide()
    }

    private fun getLoginResponse() {
        viewModel.loginResponse.observe(this) { value ->
            value?.let {
                when (it) {
                    is ApiResponseWrapper.Error -> {
                        if (it.data == null) {
                            it.exception?.localizedMessage?.let { err ->
                                setLogCat("Error_Data", err)
                                //activity?.toastMsg("Error $err")
                                showErrorDialog(err, false)
                            }
                        } else {
                            setLogCat("Error_Data", "${it.data}")
                            //activity?.toastMsg(" Error Data ${it.data}")
                            showErrorDialog("${it.data}", false)
                        }
                    }
                    is ApiResponseWrapper.Loading -> {}
                    is ApiResponseWrapper.Success -> {}
                }
            }
        }
    }

    fun showBottomNavBar() {
        binding.bottomNavBar.show()
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }


    fun logout(): Boolean {
        viewModel.removeLoginResponse()
        val sharePreference = getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        return sharePreference.edit().remove(FilesUtils.DATASTORE.LoginType).commit()
    }

    fun gotSelectionScreen(){
        startActivity(Intent(this, LoginSelectionOption::class.java))
        this.finishAffinity()
    }

    fun getBottomNav() = binding.bottomNavBar
    fun goBack() {
        onBackPressed()
    }

}