package com.uptodd.uptoddapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.facebook.login.LoginManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.uptodd.uptoddapp.databinding.ActivityUptoddWebsiteBinding
import com.uptodd.uptoddapp.databinding.DialogExtendSubscriptionBinding
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AppNetworkStatus
import com.uptodd.uptoddapp.workManager.DAILY_PRE_SALES_CHECK_WORK_MANAGER_TAG
import com.uptodd.uptoddapp.workManager.DailyPreSalesCheck
import java.util.concurrent.TimeUnit


class UptoddWebsiteActivity : AppCompatActivity() {

    lateinit var binding:ActivityUptoddWebsiteBinding

    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    var userType:String="Google"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_uptodd_website)
        binding.lifecycleOwner=this

        supportActionBar?.title="UpTodd"

        preferences = getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor= preferences!!.edit()

        if(preferences!!.contains("userType"))
            preferences!!.getString("userType","Google")

        val workManagerPreferences = getSharedPreferences("workManager", Context.MODE_PRIVATE)
        if(workManagerPreferences.getBoolean("isWorkMangerPreSalesScheduled", false)){
            fireDailyPreSalesCheckWorkManager()
            workManagerPreferences.edit().putBoolean("isWorkMangerPreSalesScheduled", true).apply()
        }

        if(AppNetworkStatus.getInstance(this).isOnline) {
            load()
        }
        else{
            val snackbar = Snackbar.make(binding.webView, getString(R.string.no_internet_connection), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry)) {
                    load()
                }
            snackbar.show()
        }

    }

    private fun fireDailyPreSalesCheckWorkManager() {
        val dailyCheckWorker = PeriodicWorkRequestBuilder<DailyPreSalesCheck>(6, TimeUnit.HOURS)
            .addTag(DAILY_PRE_SALES_CHECK_WORK_MANAGER_TAG)
            .build()

        val workManager = WorkManager.getInstance(application)
        workManager.enqueue(dailyCheckWorker)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_with_logout,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menuButton_logout)
        {
            onClickLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onClickLogout() {
        val dialogBinding=DataBindingUtil.inflate<DialogExtendSubscriptionBinding>(
            layoutInflater, R.layout.dialog_extend_subscription, null, false)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.textView.text=getString(R.string.are_you_sure_logout)
        dialogBinding.buttonYes.setOnClickListener {
            editor?.putBoolean("loggedIn", false)
            editor?.commit()

            if(userType=="Google")
                FirebaseAuth.getInstance().signOut()
            else
            {
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
            }

            AllUtil.unregisterToken()

            WorkManager.getInstance(application).cancelAllWorkByTag(
                DAILY_PRE_SALES_CHECK_WORK_MANAGER_TAG)

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        dialogBinding.buttonNo.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.show()

    }


    private fun load() {
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                title = getString(R.string.loading)
                binding.progressBar.setProgress(progress) //Make the bar disappear after URL is loaded

                // Return the app name after finish loading
                if (progress == 100)
                {
                    binding.progressBar.visibility= View.INVISIBLE
                }
            }
        }
        binding.webView.loadUrl(resources.getString(R.string.uptoddWebsiteUrl))
        binding.webView.settings.javaScriptEnabled=true
    }
}