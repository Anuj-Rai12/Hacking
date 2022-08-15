package com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentFragmentBinding
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.ui.freeparenting.content.repo.VideoContentRepository
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor.DailyContentAdaptor
import com.uptodd.uptoddapp.utils.*


class DailyContentFragment(
    private val title: String,
    private val list: List<Content>,
    private val dbContent: MutableList<Content>,
    private val bg: Pair<Int, Int>
) :
    Fragment(R.layout.daily_content_fragment),
    OnBottomClick {

    private lateinit var binding: DailyContentFragmentBinding
    private lateinit var dailVideoAdaptor: DailyContentAdaptor
    private var currentPlayingContent = list.first()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyContentFragmentBinding.bind(view)
        binding.introProgramsTxt.text = title
        binding.mainContainer.setBackgroundResource(bg.second)
        setVideoOrMusicContent(currentPlayingContent)

        binding.mainImageLayout.setOnClickListener {
            playVideo(currentPlayingContent.url)
        }

        binding.playMusic.setOnClickListener {
            showSnackBar("Working on music section")
        }
        listenForProgress()
        setWebViewSetUp()
        setAdaptor()
    }

    private fun setImageToThumnail(currentVideo: Content) {
        binding.videoThumbnail.apply {
            context.showImage(currentVideo.url, this, true)
        }
        binding.videoName.text = currentVideo.name
    }

    private fun setAdaptor() {
        binding.suggestionPlayList.apply {
            isNestedScrollingEnabled = false
            dailVideoAdaptor = DailyContentAdaptor(bg.second)
            dailVideoAdaptor.itemClickListener = this@DailyContentFragment
            adapter = dailVideoAdaptor
        }
        dailVideoAdaptor.submitList(list)
    }

    private fun showSnackBar(msg: String) {
        binding.root.showSnackBarMsg(
            msg,
            anchor = (activity as FreeParentingDemoActivity).getBottomNav()
        )
    }


    private fun getVideoThumbnail() {
        binding.videoThumbnail.visibility = View.VISIBLE
        binding.darkColorRecycle.show()
        binding.mainImageLayout.show()
        binding.mainConstraintHolder.setPadding(10)
        setWebContentToNull()
    }

    private fun setWebContentToNull() {
        binding.webViewPlayer.loadUrl("javascript:document.open();document.close();")
    }


    private fun playVideo(url: String) {
        if (checkUserInput(url)) {
            showSnackBar("Unable play it!!")
            return
        }
        binding.videoThumbnail.invisible()
        binding.darkColorRecycle.hide()
        binding.webViewPlayer.setBackgroundColor(Color.TRANSPARENT)
        binding.mainConstraintHolder.setPadding(0)
        binding.webViewPlayer.loadUrl("https://uptodd.com/playYoutubeVideos/$url?fs=0")
    }

    override fun <T> onClickListener(res: T) {
        val response = res as Content
        currentPlayingContent = response
        setVideoOrMusicContent(currentPlayingContent)
    }

    private fun setVideoOrMusicContent(response: Content) {
        when (VideoContentRepository.Companion.ItemType.valueOf(response.type)) {
            VideoContentRepository.Companion.ItemType.MUSIC -> {
                binding.mainConstraintHolder.hide()
                binding.mainMusicLayout.show()
                binding.musicFragmentLayout.show()
                setWebContentToNull()
            }
            VideoContentRepository.Companion.ItemType.VIDEO -> {
                getVideoThumbnail()
                setImageToThumnail(response)
                binding.mainConstraintHolder.show()
                binding.mainMusicLayout.hide()
            }
        }
    }


    private fun listenForProgress() {
        binding.webViewPlayer.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                setLogCat("PodcastWebinarActivity", "onProgressChanged: $newProgress")
                binding.progressCircle.show()
                if (newProgress == 100) {
                    binding.mainImageLayout.hide()
                    binding.progressCircle.hide()
                }
                super.onProgressChanged(view, newProgress)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
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
        }
    }

}