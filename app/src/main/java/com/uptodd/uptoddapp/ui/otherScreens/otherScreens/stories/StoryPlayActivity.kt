package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.stories

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityStoryPlayBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utils.*

class StoryPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryPlayBinding

    var selected = false
    var flag = false
    private var title: String? = null
    private var description: String? = null
    private var podcast: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_story_play)


        getRandomBgColor.apply {
            changeStatusBarColor(this.first)
            binding.videoViewLayout.setBackgroundResource(this.second)
        }

        val intent: Intent = intent
        podcast = intent.getStringExtra("podcast")!!
        Log.d("div", "FullWebinarActivity L93 $podcast")
        title = intent.getStringExtra("title")!!
        description = intent.getStringExtra("description")
        binding.title.text = title
        binding.description.text = description
        if (description.isNullOrEmpty()) {
            binding.description.visibility = View.GONE
        }


        "https://img.youtube.com/vi/$podcast/mqdefault.jpg".also { url ->
            Glide.with(this)
                .load(Uri.parse(url))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.videoThumbnail)
        }
        binding.bckArrow.setOnClickListener {
            onBackPressed()
        }

        binding.mainImageLayout.setOnClickListener {
            binding.videoThumbnail.invisible()
            binding.darkColorRecycle.hide()
            binding.webViewPlayer.setBackgroundColor(Color.TRANSPARENT)
            binding.mainConstraintHolder.setPadding(0)
            setWebViewSetUp()
        }

        listenForProgress()

    }


    private fun listenForProgress() {
        binding.webViewPlayer.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                Log.i("PodcastWebinarActivity", "onProgressChanged: $newProgress")
                binding.progressCircle.show()
                if (newProgress == 100) {
                    binding.mainImageLayout.hide()
                    binding.progressCircle.hide()
                }
                super.onProgressChanged(view, newProgress)
            }
        }
    }


    private fun setWebViewSetUp() {
        binding.webViewPlayer.apply {
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                javaScriptCanOpenWindowsAutomatically = true
                loadsImagesAutomatically = true
                setSupportMultipleWindows(true)
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }
            this.loadUrl("https://uptodd.com/playYoutubeVideos/$podcast?fs=0")//uxSh8svEoZQ
        }
    }

    override fun onResume() {
        supportActionBar?.hide()
        super.onResume()
    }


}