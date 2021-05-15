package com.uptodd.uptoddapp.ui.webinars.webinarslist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityWebinarsBinding

class WebinarsActivity : AppCompatActivity() {

    lateinit var binding: ActivityWebinarsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityWebinarsBinding>(this, R.layout.activity_webinars)

    }
}