package com.uptodd.uptoddapp.utilities


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.*
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.expertCounselling.TermsAndConditions
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.YouTubeConfig
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer
import java.util.concurrent.TimeUnit

class YoutubeFragment() :YouTubePlayerFragment() {

    var videoId:String?=null
    lateinit var binding:YoutubeFragmentBinding
    var selected=false
    var flag=false
    var musicPlayed=false

    var handler: Handler?=null
    private lateinit var VIDEO_SAMPLE: String
    private lateinit var title: String
    private  var description: String? = null
    private  var kitContent:String? = null

    var player: YouTubePlayer?=null

    private lateinit var mOnInitializedListener: YouTubePlayer.OnInitializedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    companion object {

        fun getInstance(videoId: String,title:String): YoutubeFragment {

            val youtubeBottomSheet = YoutubeFragment()
            val bundle = Bundle();
            bundle.putString("videoId", videoId)
            bundle.putString("title",title)
            youtubeBottomSheet.arguments = bundle

            return youtubeBottomSheet
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= YoutubeFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(UpToddMediaPlayer.isPlaying)
        {
            musicPlayed=true
            UpToddMediaPlayer.upToddMediaPlayer.playPause()
            val intent = Intent(activity, BackgroundPlayer::class.java)
            intent.putExtra("toRun", false)
            intent.putExtra("musicType", "music")
            activity.sendBroadcast(intent)
        }
        title = arguments?.getString("title","")!!
        videoId = arguments?.getString("videoId","")

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
                            p1?.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
                            p1.setShowFullscreenButton(true)
                        }

                        override fun onVideoEnded() {

                        }

                        override fun onError(p0: YouTubePlayer.ErrorReason?) {
                        }

                    })


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


    fun handleClick()
    {
        val sharedPreferences= UptoddSharedPreferences.getInstance(activity)
        val intent= Intent(Intent.ACTION_VIEW,
            Uri.parse(sharedPreferences.getOnboardingLink()))
        startActivity(intent)
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



}