package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.VideoFragmentLayoutBinding
import com.example.hackerstudent.utils.*
import com.potyvideo.library.globalInterfaces.AndExoPlayerListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoFragment : Fragment(R.layout.video_fragment_layout) {
    private lateinit var binding: VideoFragmentLayoutBinding
    private val args: VideoFragmentArgs by navArgs()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).hide()
        hideBottomNavBar()
        activity?.changeStatusBarColor(R.color.black)
        binding = VideoFragmentLayoutBinding.bind(view)
        if (!args.title.contains("Preview")) {
            context?.msg("No Recoding Allowed")
            activity?.preventScreenShotOrVideoRecoding()
        }
        binding.courseTitle.text = args.title
        binding.andExoPlayerView.setSource(args.video)
        onBackPressed()

        binding.andExoPlayerView.setAndExoPlayerListener(object : AndExoPlayerListener {
            override fun onExoPlayerError(errorMessage: String?) {
                errorMessage?.let {
                    dir(message = it)
                }
                super.onExoPlayerError(errorMessage)
            }
        })

        binding.backBtn.setOnClickListener {
            val orientation = binding.andExoPlayerView.getActivity()?.requestedOrientation
            Log.i(TAG, "onViewCreated: $orientation")
            if (orientation == GetConstStringObj.LAND_SCAPE) {
                binding.andExoPlayerView.getActivity()?.requestedOrientation =
                    GetConstStringObj.UnSpecified
            }
            if (!args.title.contains("Preview")) {
                context?.msg("Recoding Allowed")
                activity?.removedScreenShotFlagOrVideoRecoding()
            }
            findNavController().popBackStack()
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.i(TAG, "onBackPressed: on Back Pressed")
        }.handleOnBackPressed()
    }

    private fun dir(title: String = "Error", message: String = "") {
        val action = VideoFragmentDirections.actionGlobalPasswordDialog2(title, message)
        findNavController().navigate(action)
    }
}