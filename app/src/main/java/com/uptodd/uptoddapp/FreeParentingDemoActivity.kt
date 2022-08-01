package com.uptodd.uptoddapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.databinding.ActvityFreeDemoBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel.LoginViewModel
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import com.uptodd.uptoddapp.utils.getEmojiByUnicode

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
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }

}