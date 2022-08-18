package com.uptodd.uptoddapp.ui.freeparenting.daily_book.tabs

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.SeekBar
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.FreeParentingDemoActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DailyContentFragmentBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.repo.VideoContentRepository
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.adaptor.DailyContentAdaptor
import com.uptodd.uptoddapp.utils.*
import java.util.concurrent.TimeUnit


class DailyContentFragment(
    private val title: String,
    private val list: List<Content>,
    private val dbContent: MutableList<Content>,
    private val bg: Int
) :
    Fragment(R.layout.daily_content_fragment),
    OnBottomClick {

    private lateinit var binding: DailyContentFragmentBinding
    private lateinit var dailVideoAdaptor: DailyContentAdaptor
    private var currentPlayingContent = list.first()

    private val loginSingletonResponse by lazy {
        LoginSingletonResponse.getInstance()
    }
    private var music: MediaPlayer? = null
    private var isMusicPlaying: Boolean = true
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var oneTimeOnly = 0
    private val myHandler: Handler = Handler(Looper.getMainLooper())
    private var playMusicUrl: String? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DailyContentFragmentBinding.bind(view)
        binding.introProgramsTxt.text = title
//        setLogCat("TESTING_TIME"," Progress :- ${loginSingletonResponse.getProgress()}")
        binding.mainContainer.setBackgroundResource(bg)
        setVideoOrMusicContent(currentPlayingContent)

        binding.mainImageLayout.setOnClickListener {
            playVideo(currentPlayingContent.url)
        }

        binding.playMusic.setOnClickListener {
            if (music == null) {
                return@setOnClickListener
            }
            if (isMusicPlaying) {
                binding.playMusic.setImageResource(R.drawable.exo_icon_pause)
                music?.start()
            } else {
                binding.playMusic.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                music?.pause()
            }
            isMusicPlaying = !isMusicPlaying
        }
        setUpMusicProgress()

        listenForProgress()
        setWebViewSetUp()
        setAdaptor()
    }

    private fun setUpMusicProgress() {
        binding.currentSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.currentSeekBar.progress = progress
                    binding.currentMusicDuration.text = getTimeFormat(progress.toLong())
                    music?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getTimeFormat(time: Long, format: String = "%d:%d"): CharSequence {
        return String.format(
            format,
            TimeUnit.MILLISECONDS.toMinutes(time),
            TimeUnit.MILLISECONDS.toSeconds(time) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        )
    }


    private fun setMusicFile(url: String) {
        music?.release()
        oneTimeOnly = 0
        music = MediaPlayer.create(
            requireActivity(),
            Uri.parse(url)
        )
        isMusicPlaying = true
        binding.playMusic.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
        endTime = (music?.duration ?: 0).toLong()
        if (oneTimeOnly == 0) {
            binding.currentSeekBar.max = endTime.toInt()
            oneTimeOnly = 1
        }
        binding.durationMusicDuration.text = getTimeFormat(endTime)
        myHandler.postDelayed(updateSongTime, 100)
    }


    private fun setImageToThumnail(currentVideo: Content) {
        binding.videoThumbnail.apply {
            context.showImage(currentVideo.url, this, true)
        }
        binding.videoName.text = currentVideo.name
    }


    private val updateSongTime: Runnable = object : Runnable {
        @SuppressLint("DefaultLocale")
        override fun run() {
            try {
                startTime = (music?.currentPosition ?: 0).toLong()
                binding.currentMusicDuration.text = getTimeFormat(startTime)
                binding.currentSeekBar.progress = startTime.toInt()
                if (binding.currentSeekBar.progress == endTime.toInt()) {
                    binding.playMusic.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
                    "0:00".also { binding.currentMusicDuration.text = it }
                    binding.currentSeekBar.progress = 0
                    playMusicUrl?.let {
                        setMusicFile(it)
                    }
                }
                myHandler.postDelayed(this, 100)
            } catch (e: Exception) {
                setLogCat("Music_err", e.localizedMessage ?: "No ERROR")
            }
        }
    }


    private fun setAdaptor() {
        binding.suggestionPlayList.apply {
            isNestedScrollingEnabled = false
            dailVideoAdaptor = DailyContentAdaptor(bg)
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
                binding.videoName.text = response.name
                setWebContentToNull()
                if (dbContent.isNotEmpty()) {
                    val res = dbContent.find { response.name == it.name }
                    res?.let {
                        playMusicUrl = it.url
                        setMusicFile(playMusicUrl!!)
                    } ?: showSnackBar("Cannot find video file")
                } else {
                    showSnackBar("Cannot play this Music..")
                }
            }
            VideoContentRepository.Companion.ItemType.VIDEO -> {
                getVideoThumbnail()
                music?.release()
                setImageToThumnail(response)
                playMusicUrl = null
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

    override fun onResume() {
        super.onResume()
        playMusicUrl?.let {
            setMusicFile(it)
        }
    }

    override fun onPause() {
        super.onPause()
        music?.release()
    }

}