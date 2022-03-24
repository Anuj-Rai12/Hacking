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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ActivityPodcastWebinarBinding
import com.uptodd.uptoddapp.databinding.LayoutFullScreenDialogBinding
import com.uptodd.uptoddapp.databinding.LayoutTermsConditionBinding
import com.uptodd.uptoddapp.databinding.YoutubeVideoBottomsheetBinding
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.expertCounselling.TermsAndConditions
import com.uptodd.uptoddapp.ui.webinars.fullwebinar.YouTubeConfig
import com.uptodd.uptoddapp.utilities.UpToddMediaPlayer
import java.util.concurrent.TimeUnit

class YoutubeBottomSheet() :DialogFragment() {

    var videoId:String?=null
    lateinit var binding:YoutubeVideoBottomsheetBinding
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
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    companion object {

        fun getInstance(videoId: String,title:String): YoutubeBottomSheet {

            val youtubeBottomSheet = YoutubeBottomSheet()
            val bundle = Bundle();
            bundle.putString("videoId", videoId)
            bundle.putString("title",title)
            youtubeBottomSheet.arguments = bundle

            return youtubeBottomSheet
        }
        fun show(videoId: String,title:String,fragmentManager:FragmentManager)
        {
            val dialog= getInstance(videoId,title)
            dialog.show(fragmentManager, TermsAndConditions::class.java.name)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= YoutubeVideoBottomsheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = arguments?.getString("title","")!!
        videoId = arguments?.getString("videoId","")


        val youtubeFragment = YoutubeFragment.getInstance(videoId!!,title) as YouTubePlayerFragment

        val fm=childFragmentManager
        val transaction = fm.beginTransaction()


        transaction.commit()

        binding.cancelDialog.setOnClickListener {
            dismiss()
        }
    }


    fun handleClick()
    {
        val sharedPreferences= UptoddSharedPreferences.getInstance(requireContext())
        val intent= Intent(Intent.ACTION_VIEW,
            Uri.parse(sharedPreferences.getOnboardingLink()))
        startActivity(intent)
    }


}