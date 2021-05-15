package com.uptodd.uptoddapp.ui.blogs.blogslist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityBlogsBinding

class BlogsActivity : AppCompatActivity() {

    lateinit var binding: ActivityBlogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityBlogsBinding>(this, R.layout.activity_blogs)

    }
}