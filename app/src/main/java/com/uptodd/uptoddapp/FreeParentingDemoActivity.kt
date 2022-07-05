package com.uptodd.uptoddapp

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.databinding.ActvityFreeDemoBinding
import com.uptodd.uptoddapp.databinding.FreeParentingActionBarBinding
import com.uptodd.uptoddapp.utils.toastMsg

class FreeParentingDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActvityFreeDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.actvity_free_demo)

    }


    fun setUpCustomActionBar(title: String, btnClick: () -> Unit) {
        val toolBinding = FreeParentingActionBarBinding.inflate(layoutInflater)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        val lp: ActionBar.LayoutParams =
            ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT
            )
        lp.gravity = Gravity.LEFT
        supportActionBar?.setCustomView(toolBinding.root, lp)

        toolBinding.topAppBar.setContentInsetsAbsolute(0, 0)

        toolBinding.topAppBar.setNavigationOnClickListener {
            toastMsg("Working ...")
            btnClick.invoke()
        }
    }


    override fun onResume() {
        super.onResume()
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_TITLE
        supportActionBar?.setDisplayShowCustomEnabled(false)
        supportActionBar?.title = getString(R.string.app_name)
    }

}