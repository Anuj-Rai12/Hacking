package com.example.hackerstudent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.hackerstudent.databinding.ActivitySplashScreenBinding
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

const val VERSIONCODE = "0"

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private var extraDialog: ExtraDialog? = null
    private val viewModel: PrimaryViewModel by viewModels()

    companion object {
        var versionControl: VersionControl? = null
    }

    @Inject
    lateinit var networkUtils: NetworkUtils

    private var stringFlag: String? = null
    private val authInstance by lazy {
        FirebaseAuth.getInstance()
    }

    private val bouncingANIM by lazy {
        AnimationUtils.loadAnimation(this, R.anim.bouncing_animation)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.changeStatusBarColor()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        MainActivity.wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (intent != null && intent.data != null) {
            if (FirebaseAuth.getInstance().isSignInWithEmailLink(intent.data!!.toString())) {
                MainActivity.emailAuthLink = intent.data!!.toString()
            }
        }

        savedInstanceState?.let {
            stringFlag = it.getString(GetConstStringObj.USERS)
        }
        setContentView(binding.root)

        stringFlag?.let {
            val str = getPathFile(it)
            val flag = str.last().toBoolean()
            dir(1, str.first(), message = str[1], flag)
        }
        binding.mainScreenLogo.startAnimation(bouncingANIM)
        bouncingANIM.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                Log.i(TAG, "onAnimationStart: Loading Animation")
            }

            override fun onAnimationEnd(animation: Animation?) {
                waitFallWhile()
            }

            override fun onAnimationRepeat(animation: Animation?) {
                Log.e(TAG, "onAnimationRepeat: Animation")
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun waitFallWhile() {
        lifecycleScope.launch {
            delay(3000)
            if (networkUtils.isConnected()) {
                checkUpdate()
            } else {
                if (isUserIsLoginIn())
                    dir(23)//Dash Board
                else
                    dir(0)//Login Scr
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkUpdate() {
        viewModel.update.observe(this) {
            when (it) {
                is MySealed.Error -> {
                    hideProg()
                    dir(1, message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    applicationContext.msg("${it.data}")
                    showProg()
                }
                is MySealed.Success -> {
                    hideProg()
                    val info = it.data as VersionControl?
                    Log.i(TAG, "checkUpdate: $info")
                    info?.let { version ->
                        versionControl = version
                        val ver = version.version.toString()
                        if (ver != VERSIONCODE) {
                            dir(
                                1,
                                title = "Update",
                                message = GetConstStringObj.VersionNote,
                                flag = true
                            )
                        } else {
                            if (isUserIsLoginIn()) {
                                dir(23)
                            } else
                                dir(0)
                        }

                    }
                }
            }
        }
    }

    private fun hideProg() {
        binding.loadingFile.hide()
    }

    private fun showProg() {
        binding.loadingFile.show()
    }

    private fun isUserIsLoginIn() = authInstance.currentUser != null


    override fun onPause() {
        super.onPause()
        extraDialog?.dismiss()
    }

    private fun dir(
        choose: Int = 0,
        title: String = "Error",
        message: String = "",
        flag: Boolean = false
    ) {
        when (choose) {
            0 -> {
                //Login Activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            1 -> {
                stringFlag = "$title,$message,$flag"
                extraDialog = ExtraDialog(title, message, flag) {
                    stringFlag = null
                    loadUrl()
                    Log.i(TAG, "dir: Item Clicked")
                    Unit
                }
                extraDialog?.isCancelable = false
                extraDialog?.show(supportFragmentManager, GetConstStringObj.USERS)
            }
            else -> {
                //User Screen
                val intent = Intent(this, ClientActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadUrl() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(versionControl?.updateurl!!))
        startActivity(browserIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stringFlag?.let {
            outState.putString(GetConstStringObj.USERS, it)
        }
    }
}