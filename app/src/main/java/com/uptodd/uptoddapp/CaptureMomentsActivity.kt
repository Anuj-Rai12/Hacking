package com.uptodd.uptoddapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.databinding.ActivityCaptureMomentsBinding

class CaptureMomentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityCaptureMomentsBinding>(this, R.layout.activity_capture_moments)

    }
}