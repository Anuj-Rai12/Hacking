package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.stories

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityStoryPlayBinding
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.YouTubeConfig
import com.uptodd.uptoddapp.utilities.ChangeLanguage
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer
import java.util.concurrent.TimeUnit

class StoryPlayActivity : YouTubeBaseActivity() {

    private lateinit var binding: ActivityStoryPlayBinding

    var selected=false
    var flag=false
    private var title: String? = null
    private var description: String? = null
    private var podcast: String? = null
    var player: YouTubePlayer?=null
    var handler: Handler?=null
    var musicPlayed=false
    private lateinit var mOnInitializedListener: YouTubePlayer.OnInitializedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ChangeLanguage(this).setLanguage()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_story_play)
        val intent: Intent = intent
        podcast = intent.getStringExtra("podcast")!!
        Log.d("div", "FullWebinarActivity L93 $podcast")
        title = intent.getStringExtra("title")!!
        description=intent.getStringExtra("description")

        if(UpToddMediaPlayer.isPlaying)
        {
            musicPlayed=true
            UpToddMediaPlayer.upToddMediaPlayer.playPause()
            val intent = Intent(this, BackgroundPlayer::class.java)
            intent.putExtra("toRun", false)
            intent.putExtra("musicType", "music")
            this.sendBroadcast(intent)
        }

        binding.title.text = title
        binding.description.text=description
        if(description.isNullOrEmpty()){
            binding.description.visibility= View.GONE
        }
        handler= Handler(Looper.getMainLooper())

        binding.seekBar.setOnSeekBarChangeListener(seekBarChangeListener)


        mOnInitializedListener = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                p1: YouTubePlayer?,
                p2: Boolean,
            ) {



                if (p1 != null) {

                    player=p1
                    p1.setPlaybackEventListener(playBackChangeListener)

                    p1.loadVideo(podcast)

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
                            p1?.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
                            p1.setShowFullscreenButton(true)
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

    var playBackChangeListener=object: YouTubePlayer.PlaybackEventListener
    {
        override fun onPlaying() {
            handler?.postDelayed(runnable,100)
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

            handler?.postDelayed(runnable,100)
        }

    }

    fun displayTime()
    {
        player.let {

            val time= it?.durationMillis?.minus(it?.currentTimeMillis!!)
            val ftext="${time?.toLong()?.let { it1 -> TimeUnit.MILLISECONDS.toMinutes(it1) }} : ${time?.toLong()?.let { it1 ->
                TimeUnit.MILLISECONDS.toSeconds(it1)%60
            }}"

            player.let {

                val total= TimeUnit.MILLISECONDS.toSeconds(it!!.durationMillis.toLong())

                val occ= TimeUnit.MILLISECONDS.toSeconds(it!!.currentTimeMillis.toLong())


                val per=((occ/total.toFloat())*100).toInt()
                Log.d("per","${((occ/total.toFloat())*100).toInt()}   $per")
                flag=true
                binding.seekBar?.progress=per.toInt()
                flag=false
                binding.videoTime.text = ftext
            }



        }
    }

    var runnable= object :Runnable{

        override fun run() {
            displayTime()
            handler?.postDelayed(this,100)
        }



    }

    var seekBarChangeListener=object : SeekBar.OnSeekBarChangeListener
    {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            player.let {
                if(p2)
                {
                    val per=((it!!.durationMillis *p1)/100)
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