package com.uptodd.uptoddapp

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.databinding.ActivityLoginBinding
import com.uptodd.uptoddapp.ui.upgrade.UpgradeFragment

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
//        setContentView(R.layout.activity_login)
        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
    }
    override fun onBackPressed() {


        if(UpgradeFragment.over)
        {
            finish()
        }
        else
        {
            super.onBackPressed()
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        Log.d("div","LoginActivity L23")
        val fragment: android.app.Fragment? =fragmentManager.findFragmentById(R.id.loginFragment)
        fragment?.onActivityResult(requestCode, resultCode, data);
    }*/
}