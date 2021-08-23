package com.uptodd.uptoddapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.databinding.ActivityLoginBinding
import com.uptodd.uptoddapp.ui.other.FragmentUpdateApp
import com.uptodd.uptoddapp.ui.upgrade.UpgradeFragment

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
//        setContentView(R.layout.activity_login)
        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        hasStoragePermission()
    }
    override fun onBackPressed() {


        if(UpgradeFragment.over || FragmentUpdateApp.isOutDated)
        {
            finish()
        }
        else
        {
            super.onBackPressed()
        }
    }

    private fun hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                   200
                )
            } else {

            }
        } else {

        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        Log.d("div","LoginActivity L23")
        val fragment: android.app.Fragment? =fragmentManager.findFragmentById(R.id.loginFragment)
        fragment?.onActivityResult(requestCode, resultCode, data);
    }*/
}