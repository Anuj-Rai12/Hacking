package com.uptodd.uptoddapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.databinding.ActvityFreeDemoBinding

class FreeParentingDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActvityFreeDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.actvity_free_demo)

    }


    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
    }

}