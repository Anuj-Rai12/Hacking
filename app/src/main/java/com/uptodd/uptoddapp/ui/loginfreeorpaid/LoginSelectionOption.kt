package com.uptodd.uptoddapp.ui.loginfreeorpaid

import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LoginOptionSelectionLayoutBinding
import com.uptodd.uptoddapp.ui.freeparenting.purchase.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.ui.loginfreeorpaid.tabs.CourseInfoFragment
import com.uptodd.uptoddapp.utils.changeStatusBarColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginSelectionOption : AppCompatActivity() {

    private lateinit var binding: LoginOptionSelectionLayoutBinding
    private lateinit var viewPagerAdaptor: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.login_option_selection_layout)
        //binding.termAndCondition.append(getString(R.string.term_and_Codition))
        val first = "By processing you agree to our<br>"
        val sec = "<font color='#2ba0c4'>Terms & Condition</font>"
        val third = " and "
        val forth = "<font color='#2ba0c4'>privacy policy</font>"
        binding.termAndCondition.text = Html.fromHtml(first + sec + third + forth)
        setAdaptor()
        viewPagerAdaptor.setFragment(CourseInfoFragment("Testing 1", R.drawable.app_icon_image))
        viewPagerAdaptor.setFragment(CourseInfoFragment("Testing 2", R.drawable.doctor_referral))
        viewPagerAdaptor.setFragment(CourseInfoFragment("Testing 3", R.drawable.app_icon))

        TabLayoutMediator(
            binding.tabItemForIntroScreen,
            binding.viewPagerMainLogin
        ) { _, _ -> }.attach()



        binding.goToFreeLoginScreen.setOnClickListener {
            startActivity(Intent(this, FreeParentingDemoActivity::class.java))
        }

        binding.goToPaidLoginScreen.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            this.finishAffinity()
        }
        movingOnScreenToAnother()
    }

    private fun movingOnScreenToAnother() {
        lifecycleScope.launch {
            var pos = 0
            while (true) {
                delay(3000)
                pos = (pos % 3)
                binding.viewPagerMainLogin.currentItem = pos
                pos++
            }
        }
    }

    private fun setAdaptor() {
        viewPagerAdaptor = ViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPagerMainLogin.adapter = viewPagerAdaptor
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        changeStatusBarColor(R.color.grey_color)
    }
}