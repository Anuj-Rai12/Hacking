package com.uptodd.uptoddapp.ui.webinars.fullwebinar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityFullWebinarBinding
import com.uptodd.uptoddapp.utilities.ChangeLanguage


class FullWebinarActivity : AppCompatActivity() {

    lateinit var binding: ActivityFullWebinarBinding
    lateinit var viewModel: FullWebinarViewModel

    private lateinit var VIDEO_SAMPLE: String
    private lateinit var title: String


    //private lateinit var mOnInitializedListener: YouTubePlayer.OnInitializedListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ChangeLanguage(this).setLanguage()
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_full_webinar
        )

        val intent: Intent = intent
        VIDEO_SAMPLE = intent.getStringExtra("url")!!
        Log.d("div", "FullWebinarActivity L93 $VIDEO_SAMPLE")
        title = intent.getStringExtra("title")!!

        binding.title.text = title


      /*  mOnInitializedListener = object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                p0: YouTubePlayer.Provider?,
                p1: YouTubePlayer?,
                p2: Boolean, ) {
                if (p1 != null) {
                    p1.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
                    p1.setShowFullscreenButton(true)
                    p1.loadVideo(VIDEO_SAMPLE)
                }
            }

            override fun onInitializationFailure(
                p0: YouTubePlayer.Provider?,
                p1: YouTubeInitializationResult?, ) {
            }
        }
*/
  //      binding.videoView.initialize(YouTubeConfig().getApiKey(), mOnInitializedListener)


    }
}