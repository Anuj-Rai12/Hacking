package com.uptodd.uptoddapp.ui.webinars.podcastwebinar

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.databinding.ActivityPodcastWebinarBinding
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.webinars.adapters.SuggestedVideoAdapter
import com.uptodd.uptoddapp.ui.webinars.adapters.SuggestedVideoInterface
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarViewModel
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utils.*


class PodcastWebinarActivity : AppCompatActivity(), SuggestedVideoInterface {

    lateinit var binding: ActivityPodcastWebinarBinding
    lateinit var viewModel: FullWebinarViewModel
    var selected = false
    var flag = false
    private var VIDEO_SAMPLE: String? = null
    private lateinit var title: String
    private var description: String? = null
    private var kitContent: String? = null
    private lateinit var videos: MutableList<ActivitySample>
    private lateinit var model: SuggestedVideosModel
    private val adapter = SuggestedVideoAdapter(this)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_podcast_webinar)


        getRandomBgColor.apply {
            changeStatusBarColor(this.first)
            binding.videoViewLayout.setBackgroundResource(this.second)
        }
        val intent: Intent = intent
        VIDEO_SAMPLE = intent.getStringExtra("url")

        Log.d("div", "FullWebinarActivity L93 $VIDEO_SAMPLE")
        title = intent.getStringExtra("title")!!
        description = intent.getStringExtra("description")
        kitContent = intent.getStringExtra("kit_content")

        videos = mutableListOf()

        model = try {
            intent.getSerializableExtra("videos") as SuggestedVideosModel
        } catch (e: Exception) {
            SuggestedVideosModel(videos)
        }

        videos = model.videos

        if (videos.size > 0) {
            binding.suggestedVideoTxt.visibility = View.VISIBLE
            binding.sugVideoRecView.visibility = View.VISIBLE
            val list: MutableList<ActivitySample> = mutableListOf()
            var count = 0
            for (video in videos) {
                if (video.title != title) {
                    list.add(video)
                    count++
                }
                if (count == 3) {
                    break
                }
            }
            adapter.list = list
            binding.sugVideoRecView.adapter = adapter
        } else {
            binding.suggestedVideoTxt.visibility = View.GONE
            binding.sugVideoRecView.visibility = View.GONE
        }


        binding.title.text = title
        binding.description.text = description
        "Home material: $kitContent".also {
            binding.kitContent.text = it
        }
        if (description.isNullOrEmpty()) {
            binding.description.visibility = View.GONE
        }
        if (kitContent.isNullOrEmpty()) {
            binding.kitContent.visibility = View.GONE
        }
        "https://img.youtube.com/vi/$VIDEO_SAMPLE/mqdefault.jpg".also { url ->
            Glide.with(this)
                .load(Uri.parse(url))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.videoThumbnail)
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
            this.loadUrl("https://uptodd.com/playYoutubeVideos/$VIDEO_SAMPLE?fs=0")//uxSh8svEoZQ
        }
    }

    override fun onResume() {
        supportActionBar?.hide()
        super.onResume()
    }


    override fun onClick(act_sample: ActivitySample) {
        val intent = Intent(this, PodcastWebinarActivity::class.java)
        intent.putExtra("url", act_sample.video)
        intent.putExtra("title", act_sample.title)
        intent.putExtra("videos", SuggestedVideosModel(videos))
        startActivity(intent)
        finishAffinity()
    }
}
