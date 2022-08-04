package com.uptodd.uptoddapp.ui.freeparenting.content

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.SeekBar
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DemoVideoContentLayoutBinding
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.ui.freeparenting.content.tabs.FreeDemoVideoModuleFragments
import com.uptodd.uptoddapp.ui.freeparenting.content.viewmodel.VideoContentViewModel
import com.uptodd.uptoddapp.ui.freeparenting.content.viewpager.ViewPagerAdapter
import com.uptodd.uptoddapp.utils.*
import com.uptodd.uptoddapp.utils.dialog.bottom_sheet.DemoBottomSheet
import com.uptodd.uptoddapp.utils.dialog.bottom_sheet.DemoBottomSheet.Companion.OptionSelection.*
import com.uptodd.uptoddapp.utils.dialog.showDialogBox
import java.util.concurrent.TimeUnit


class DemoVideoContentFragment : Fragment(R.layout.demo_video_content_layout), OnBottomClick {
    private lateinit var binding: DemoVideoContentLayoutBinding
    private var viewPagerAdaptor: ViewPagerAdapter? = null

    private val viewModel: VideoContentViewModel by viewModels()
    private val allDownloadFile = mutableListOf<Content>()
    private val allVideoFetchFile = mutableListOf<Content>()
    private var newVideo: String = ""
    private var music: MediaPlayer? = null
    private var isMusicPlaying: Boolean = true
    private val myHandler: Handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var oneTimeOnly = 0
    private var playMusicUrl: String? = null

    private val loginSingletonResponse by lazy {
        LoginSingletonResponse.getInstance()
    }

    private var progressCounter = 8//loginSingletonResponse.getProgress()


    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DemoVideoContentLayoutBinding.bind(view)
        "Video content is good testing sample url!!".also { binding.videoTitle.text = it }
        setLogCat("Demo_FREE", "Progress is $progressCounter")
        setAdaptor()
        getAllItemFromDb()
        getVideContentResponse()
        setWebViewSetUp()
        listenForProgress()
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
        binding.bckArrow.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.mainImageLayout.setOnClickListener {
            playVideo()
        }

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


        binding.nextBtn.setOnClickListener {
            Log.i(
                "Demo_FREE",
                "onViewCreated: ${allVideoFetchFile.last()} and  Progress counter is $progressCounter"
            )
            val isInLastVideo = allVideoFetchFile.last().id == progressCounter
            if (!isInLastVideo) {
                showBottomSheet()
            } else {
                activity?.showDialogBox(
                    "Course Completed",
                    "Congratulations for completing course ${getEmojiByUnicode(0x1F389)}${
                        getEmojiByUnicode(
                            0x1F389
                        )
                    }", "thanks"
                ) {}
            }
        }

    }

    private fun showBottomSheet() {
        val openBottomSheetDialog =
            DemoBottomSheet("\nWe recommend watching the complete video as it will help you understand how to prepare for parenthood.\n")
        openBottomSheetDialog.onListener = this
        openBottomSheetDialog.show(childFragmentManager, "Open Bottom Sheet")
    }

    private fun playVideo() {
        if (checkUserInput(newVideo)) {
            activity?.toastMsg("Oops Something Went Wrong!!")
            return
        }
        binding.videoThumbnail.invisible()
        binding.darkColorRecycle.hide()
        binding.webViewPlayer.setBackgroundColor(Color.TRANSPARENT)
        binding.mainConstraintHolder.setPadding(0)
        binding.webViewPlayer.loadUrl("https://uptodd.com/playYoutubeVideos/$newVideo?fs=0")
    }

    @Suppress("UNCHECKED_CAST")
    private fun getAllItemFromDb() {
        viewModel.getVideoContentResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    setLogCat("DB_VIDEO", "${it.exception}")
                }
                is ApiResponseWrapper.Loading -> {
                    setLogCat("DB_VIDEO", "${it.data}")
                }
                is ApiResponseWrapper.Success -> {
                    setLogCat("DB_VIDEO", "${it.data}")
                    if (it.data !is String) {
                        val item = it.data as List<Content>
                        allDownloadFile.addAll(item)
                    }
                }
            }
        }
    }

    //"/storage/emulated/0/Android/data/com.uptodd.uptoddapp/files/Music/Downloads/FreeParenting/FLOWINGWATERSTIMULATION.acc"
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

    override fun onPause() {
        super.onPause()
        music?.release()
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

    private fun getTimeFormat(time: Long, format: String = "%d:%d"): CharSequence {
        return String.format(
            format,
            TimeUnit.MILLISECONDS.toMinutes(time),
            TimeUnit.MILLISECONDS.toSeconds(time) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
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
        binding.nextBtn.hide()
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
        binding.nextBtn.show()
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

        if (video.id == progressCounter + 1) {
            showBottomSheet()
            return
        }

        if (video.id > progressCounter) {
            activity?.showDialogBox(
                "Video Locked",
                "We recommend watching the complete video before moving to other as it will be more fruitful follow course in series"
            ) {}
            return
        }

        when (FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.valueOf(video.type)) {
            FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.VIDEO -> {
                music?.release()
                binding.mainConstraintHolder.show()
                newVideo = video.url
                getVideoThumbnail()
                showImage(newVideo)
                binding.mainMusicLayout.hide()
            }
            FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.MUSIC -> {
                binding.mainConstraintHolder.hide()
                setWebContentToNull()
                binding.mainMusicLayout.show()
                if (allDownloadFile.isNotEmpty()) {
                    val res = allDownloadFile.find { it.name == video.name }
                    res?.let { content ->
                        playMusicUrl = content.url
                        setMusicFile(playMusicUrl!!)
                    } ?: run {
                        activity?.toastMsg("Error with the url")
                    }
                } else {
                    activity?.toastMsg("Cannot play this video..")
                }
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

    private fun getVideContentResponse() {
        viewModel.videoContentResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResponseWrapper.Error -> {

                }
                is ApiResponseWrapper.Loading -> {}
                is ApiResponseWrapper.Success -> {

                    val res = it.data as VideoContentList?
                    res?.let { video ->
                        video.data.forEach { data ->
                            allVideoFetchFile.addAll(data.content)
                        }
                        var content = allVideoFetchFile.find { con ->
                            con.id == progressCounter
                        }
                        if (content == null) {
                            content = allVideoFetchFile.find { con ->
                                con.id == ((progressCounter - 1))
                            }
                        }
                        setNewVideo(content!!)

                    } ?: kotlin.run {
                        activity?.toastMsg("Video Content error")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getVideoContentItem()
        viewModel.getVideoContent()
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

    override fun <T> onClickListener(res: T) {
        if (res is String) {
            val type = res as String

            when (valueOf(type)) {
                RESUME -> {
                    playVideo()
                }
                NEXT -> {
                    val findVideo = allVideoFetchFile.find {
                        it.id == progressCounter + 1
                    }
                    if (findVideo != null) {
                        progressCounter += 1
                        setNewVideo(findVideo)
                    }
                }
            }
        }
    }

}