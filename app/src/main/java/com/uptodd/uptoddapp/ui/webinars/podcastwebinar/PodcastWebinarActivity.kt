package com.uptodd.uptoddapp.ui.webinars.podcastwebinar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityPodcastWebinarBinding
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.FullWebinarViewModel
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.YouTubeConfig
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer

class PodcastWebinarActivity: YouTubeBaseActivity() {

    lateinit var binding: ActivityPodcastWebinarBinding
    lateinit var viewModel: FullWebinarViewModel
    var selected=false
    var musicPlayed=false

    private lateinit var VIDEO_SAMPLE: String
    private lateinit var title: String
    private lateinit var description: String
    private lateinit var kitContent:String

    private lateinit var mOnInitializedListener: YouTubePlayer.OnInitializedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_podcast_webinar
        )

        if(UpToddMediaPlayer.isPlaying)
        {
            musicPlayed=true
            UpToddMediaPlayer.upToddMediaPlayer.playPause()
            val intent = Intent(this, BackgroundPlayer::class.java)
            intent.putExtra("toRun", false)
            intent.putExtra("musicType", "music")
            this.sendBroadcast(intent)
        }
        val intent: Intent = intent
        VIDEO_SAMPLE = intent.getStringExtra("url")!!
        Log.d("div", "FullWebinarActivity L93 $VIDEO_SAMPLE")
        title = intent.getStringExtra("title")!!
        description=intent.getStringExtra("description")!!
        kitContent=intent.getStringExtra("kit_content")!!

        binding.title.text = title
        binding.description.text=description
        binding.kitContent.text="Kit content: $kitContent"

        mOnInitializedListener = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                p1: YouTubePlayer?,
                p2: Boolean, ) {

                if (p1 != null) {
                    p1.loadVideo(VIDEO_SAMPLE)
                    p1.setPlayerStateChangeListener(object :YouTubePlayer.PlayerStateChangeListener
                    {
                        override fun onAdStarted() {

                        }

                        override fun onLoading() {

                        }

                        override fun onVideoStarted() {

                        }

                        override fun onLoaded(p0: String?) {
                            p1.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
                            p1.setShowFullscreenButton(true)
                           p1.pause()
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

    override fun onDestroy() {
        if(!UpToddMediaPlayer.isPlaying)
        {
            if(UpToddMediaPlayer.songPlaying!=null && musicPlayed)
            {
                UpToddMediaPlayer.upToddMediaPlayer.playPause()
                val intent = Intent(this, BackgroundPlayer::class.java)
                intent.putExtra("toRun", true)
                intent.putExtra("musicType", "music")
                this.sendBroadcast(intent)
            }

        }
        super.onDestroy()

    }
}
