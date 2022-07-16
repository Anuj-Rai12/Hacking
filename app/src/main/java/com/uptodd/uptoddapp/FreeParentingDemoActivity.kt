package com.uptodd.uptoddapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.databinding.ActvityFreeDemoBinding

class FreeParentingDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActvityFreeDemoBinding
    private lateinit var navController: NavController

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
            graph.startDestination = R.id.freeDemoBashBoardFragment
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