package com.uptodd.uptoddapp.ui.login.googlelogin

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.UptoddWebsiteActivity
import com.uptodd.uptoddapp.databinding.ActivityGoogleLoginBinding
import com.uptodd.uptoddapp.databinding.DialogExtendSubscriptionBinding
import com.uptodd.uptoddapp.workManager.cancelAllWorkManagers

class GoogleLoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityGoogleLoginBinding

    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_google_login)

        preferences=getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
        editor = preferences!!.edit()

        binding.buttonWebsite.setOnClickListener { onClickWebsite() }
        binding.imageButtonLogout.setOnClickListener { onClickLogout() }

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

            cancelAllWorkManagers(this.application,this)

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        dialogBinding.buttonNo.setOnClickListener { dialog.dismiss() }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun onClickWebsite() {
        startActivity(Intent(this, UptoddWebsiteActivity::class.java))
    }
}