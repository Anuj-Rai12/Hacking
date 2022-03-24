package com.uptodd.uptoddapp.utilities

import android.content.Context
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import com.google.android.material.appbar.AppBarLayout
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.LayoutCollapaseToolbarBinding
import kotlin.math.abs

class ToolbarUtils {
    companion object{

        fun initToolbar(activity: Context,
                        binding:LayoutCollapaseToolbarBinding, navController: NavController,
                        title:String, subtitle:String,resId:Int
                        ) {

            val appCompatActivity = activity as AppCompatActivity
            appCompatActivity.setSupportActionBar(binding.toolbar)
            appCompatActivity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_backspace_24)
            appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
           // appCompatActivity.supportActionBar?.title = title
            binding.rootToolbar.isTitleEnabled=false
            appCompatActivity.supportActionBar?.title = title
            binding.toolbarIcon.setImageResource(resId)
            binding.toolbar.setNavigationOnClickListener {
                navController.navigateUp()
            }
            binding.tvTitle.text = title
            binding.tvSubTitle.text = subtitle
            binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.
            OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if(abs(verticalOffset) - appBarLayout?.totalScrollRange!! ==0){
                    appCompatActivity.supportActionBar?.title = title
                } else {

                    appCompatActivity.supportActionBar?.title=""
                }
            })



        }

        fun initNCToolbar(activity: Context,title: String,toolbar: Toolbar,navController: NavController){
            val appCompatActivity = activity as AppCompatActivity
            appCompatActivity.setSupportActionBar(toolbar)
            appCompatActivity.supportActionBar?.show()
            appCompatActivity.supportActionBar?.title=title
            appCompatActivity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_backspace_24)
            appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setNavigationOnClickListener {
                navController.navigateUp()
            }
        }
    }
}