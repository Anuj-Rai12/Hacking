package com.example.hackerstudent.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.VideoFragmentLayoutBinding
import com.example.hackerstudent.utils.hide
import com.example.hackerstudent.utils.hideBottomNavBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoFragment : Fragment(R.layout.video_fragment_layout) {
    private lateinit var binding: VideoFragmentLayoutBinding
    private val args: VideoFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).hide()
        hideBottomNavBar()
        binding = VideoFragmentLayoutBinding.bind(view)
        binding.andExoPlayerView.setSource(args.video)
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}