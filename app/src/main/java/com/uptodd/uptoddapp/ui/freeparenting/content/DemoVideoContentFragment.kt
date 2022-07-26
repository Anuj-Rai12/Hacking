package com.uptodd.uptoddapp.ui.freeparenting.content

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoVideoContentLayoutBinding
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.ui.freeparenting.content.tabs.FreeDemoVideoModuleFragments
import com.uptodd.uptoddapp.ui.freeparenting.content.viewmodel.VideoContentViewModel
import com.uptodd.uptoddapp.ui.freeparenting.content.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.utils.*

class DemoVideoContentFragment : Fragment(R.layout.demo_video_content_layout) {
    private lateinit var binding: DemoVideoContentLayoutBinding
    private var viewPagerAdaptor: ViewPagerAdapter? = null

    private val viewModel: VideoContentViewModel by viewModels()

    private var newVideo: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DemoVideoContentLayoutBinding.bind(view)
        "Video content is good testing sample url!!".also { binding.videoTitle.text = it }
        setAdaptor()
        showImage("uSTCoECm3TA")
        binding.mainImageLayout.setOnClickListener {
            binding.videoThumbnail.invisible()
            binding.darkColorRecycle.hide()
            binding.webViewPlayer.setBackgroundColor(Color.TRANSPARENT)
            binding.mainConstraintHolder.setPadding(0)
            setWebViewSetUp(newVideo)
        }

        listenForProgress()

    }

    private fun showImage(id: String) {
        "https://img.youtube.com/vi/$id/mqdefault.jpg".also { url ->
            Glide.with(this)
                .load(Uri.parse(url))
                .transform(CenterCrop(), RoundedCorners(20))
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.default_set_android_thumbnail)
                .into(binding.videoThumbnail)
        }
    }

    private fun setAdaptor() {
        viewPagerAdaptor = ViewPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        setFragment(FreeDemoVideoModuleFragments())
    }


    private fun changeLayoutOnLandScape() {

        val param = binding.videoViewLayout.layoutParams
        param.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.mainImageLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        binding.mainConstraintHolder.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.videoThumbnail.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.musicFragmentLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.mainMusicLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

    }


    private fun changeLayoutOnPortrait() {

        val param = binding.videoViewLayout.layoutParams
        param.height = ViewGroup.LayoutParams.WRAP_CONTENT

        binding.mainImageLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        binding.mainConstraintHolder.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        binding.videoThumbnail.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        binding.musicFragmentLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.mainMusicLayout.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeLayoutOnLandScape()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            changeLayoutOnPortrait()
        }
    }

    fun setNewVideo(video: Content) {
        when (FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.valueOf(video.type)) {
            FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.VIDEO -> {
                binding.mainConstraintHolder.show()
                newVideo=video.url
                if (binding.videoThumbnail.isVisible){
                    showImage(newVideo)
                }else{
                    setWebViewSetUp(newVideo)
                }
                binding.mainMusicLayout.hide()
            }
            FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.MUSIC -> {
                binding.mainConstraintHolder.hide()
                binding.mainMusicLayout.show()
            }
        }
        binding.videoTitle.text = video.name
    }

    private fun setFragment(fragment: Fragment): Int? {
        viewPagerAdaptor?.setFragment(fragment)
        binding.viewPager.adapter = viewPagerAdaptor
        return viewPagerAdaptor?.getSize()
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


    private fun setWebViewSetUp(id:String) {
        if (checkUserInput(newVideo)) {
            activity?.toastMsg("Oops Something Went Wrong!!")
            return
        }
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
            this.loadUrl("https://uptodd.com/playYoutubeVideos/$id?fs=0")//uxSh8svEoZQ
        }
    }

}