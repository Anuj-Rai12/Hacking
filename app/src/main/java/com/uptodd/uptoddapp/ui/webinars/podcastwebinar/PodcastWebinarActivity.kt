package com.uptodd.uptoddapp.ui.webinars.podcastwebinar

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.activitysample.ActivitySample
import com.uptodd.uptoddapp.databinding.ActivityPodcastWebinarBinding
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.ui.todoScreens.viewPagerScreens.models.SuggestedVideosModel
import com.uptodd.uptoddapp.ui.webinars.adapters.SuggestedVideoAdapter
import com.uptodd.uptoddapp.ui.webinars.adapters.SuggestedVideoInterface
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarViewModel
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.YouTubeConfig
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer
import com.uptodd.uptoddapp.utils.changeStatusBarColor
import com.uptodd.uptoddapp.utils.getRandomBgColor
import java.util.concurrent.TimeUnit


class PodcastWebinarActivity : YouTubeBaseActivity(), SuggestedVideoInterface {

    lateinit var binding: ActivityPodcastWebinarBinding
    lateinit var viewModel: FullWebinarViewModel
    var selected = false
    var flag = false
    var musicPlayed = false

    var handler: Handler? = null
    private var VIDEO_SAMPLE: String?=null
    private lateinit var title: String
    private var description: String? = null
    private var kitContent: String? = null
    private lateinit var videos: MutableList<ActivitySample>
    private lateinit var model: SuggestedVideosModel
    var player: YouTubePlayer? = null
    private val adapter = SuggestedVideoAdapter(this)

    private lateinit var mOnInitializedListener: YouTubePlayer.OnInitializedListener


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_podcast_webinar)


        getRandomBgColor.apply {
            changeStatusBarColor(this.first)
            binding.videoViewLayout.setBackgroundResource(this.second)
        }


        if (UpToddMediaPlayer.isPlaying) {
            musicPlayed = true
            UpToddMediaPlayer.upToddMediaPlayer.playPause()
            val intent = Intent(this, BackgroundPlayer::class.java)
            intent.putExtra("toRun", false)
            intent.putExtra("musicType", "music")
            this.sendBroadcast(intent)
        }
        val intent: Intent = intent
        VIDEO_SAMPLE = intent.getStringExtra("url")

        Log.d("div", "FullWebinarActivity L93 $VIDEO_SAMPLE")
        title = intent.getStringExtra("title")!!
        description = intent.getStringExtra("description")
        kitContent = intent.getStringExtra("kit_content")

        videos = mutableListOf()

        try {
            model = intent.getSerializableExtra("videos") as SuggestedVideosModel
        } catch (e: Exception) {
            model = SuggestedVideosModel(videos)
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
        binding.kitContent.text = "Home material: $kitContent"
        if (description.isNullOrEmpty()) {
            binding.description.visibility = View.GONE
        }
        if (kitContent.isNullOrEmpty()) {
            binding.kitContent.visibility = View.GONE
        }
        handler = Handler(Looper.getMainLooper())

        binding.seekBar.setOnSeekBarChangeListener(seekBarChangeListener)


        mOnInitializedListener = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                p1: YouTubePlayer?,
                p2: Boolean,
            ) {


                if (p1 != null) {

                    player = p1
                    p1.setPlaybackEventListener(playBackChangeListener)

                    p1.loadVideo(VIDEO_SAMPLE)

                    p1.setPlayerStateChangeListener(object :
                        YouTubePlayer.PlayerStateChangeListener {
                        override fun onAdStarted() {

                        }

                        override fun onLoading() {

                        }

                        override fun onVideoStarted() {
                            displayTime()


                        }

                        override fun onLoaded(p0: String?) {
                            displayTime()
                            p1.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
                            //p1.setShowFullscreenButton(true)
                        }

                        override fun onVideoEnded() {

                        }

                        override fun onError(p0: YouTubePlayer.ErrorReason?) {
                        }

                    })

                    binding.btnPlayPause.setOnClickListener {

                        selected = if (!selected) {
                            p1.play()
                            binding.btnPlayPause.text = "Pause"
                            !selected
                        } else {
                            p1.pause()
                            binding.btnPlayPause.text = "Play"
                            !selected
                        }
                    }
                }
            }

            override fun onInitializationFailure(
                p0: YouTubePlayer.Provider?,
                p1: YouTubeInitializationResult?
            ) {

            }
        }


        binding.videoView.initialize(YouTubeConfig().getApiKey(), mOnInitializedListener)
    }


    var playBackChangeListener = object : YouTubePlayer.PlaybackEventListener {
        override fun onPlaying() {
            handler?.postDelayed(runnable, 100)
            displayTime()


        }

        override fun onPaused() {
            handler?.removeCallbacks(runnable)

        }

        override fun onStopped() {

            handler?.removeCallbacks(runnable)
        }

        override fun onBuffering(p0: Boolean) {

        }

        override fun onSeekTo(p0: Int) {

            handler?.postDelayed(runnable, 100)
        }

    }

    fun displayTime() {
        player?.let {yt2->

            val time = yt2.durationMillis.minus(yt2.currentTimeMillis)
            val ftext = "${time.toLong().let { it1 -> TimeUnit.MILLISECONDS.toMinutes(it1) }} : ${
                time.toLong().let { it1 ->
                    TimeUnit.MILLISECONDS.toSeconds(it1) % 60
                }
            }"

            player?.let { yt ->

                val total = TimeUnit.MILLISECONDS.toSeconds(yt.durationMillis.toLong())

                val occ = TimeUnit.MILLISECONDS.toSeconds(yt.currentTimeMillis.toLong())


                val per = ((occ / total.toFloat()) * 100).toInt()
                Log.d("per", "${((occ / total.toFloat()) * 100).toInt()}   $per")
                flag = true
                binding.seekBar.progress = per
                flag = false
                binding.videoTime.text = ftext
            } ?: Log.i("PlayerList", "Player is Null")


        }
    }

    var runnable = object : Runnable {

        override fun run() {
            displayTime()
            handler?.postDelayed(this, 100)
        }


    }

    var seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            player.let {
                if (p2) {
                    val per = ((it!!.durationMillis * p1) / 100)
                    it.seekToMillis(per)
                }

            }
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {

        }

    }


    override fun onDestroy() {
        if (!UpToddMediaPlayer.isPlaying) {
            if (UpToddMediaPlayer.songPlaying != null && musicPlayed) {
                UpToddMediaPlayer.upToddMediaPlayer.playPause()
                val intent = Intent(this, BackgroundPlayer::class.java)
                intent.putExtra("toRun", true)
                intent.putExtra("musicType", "music")
                this.sendBroadcast(intent)
            }

        }
        super.onDestroy()

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
