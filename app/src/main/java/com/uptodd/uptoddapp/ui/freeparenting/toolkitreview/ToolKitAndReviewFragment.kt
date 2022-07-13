package com.uptodd.uptoddapp.ui.freeparenting.toolkitreview

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.TookKitAndReviewLayoutBinding
import com.uptodd.uptoddapp.datamodel.toolkit.VideoForReview
import com.uptodd.uptoddapp.ui.freeparenting.toolkitreview.adaptor.ToolKitAndReviewAdaptor
import com.uptodd.uptoddapp.utils.toastMsg

class ToolKitAndReviewFragment : Fragment(R.layout.took_kit_and_review_layout) {
    private lateinit var binding: TookKitAndReviewLayoutBinding

    private lateinit var toolKitAdaptor: ToolKitAndReviewAdaptor
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = TookKitAndReviewLayoutBinding.bind(view)
        setAdaptor()
    }

    private fun setAdaptor() {
        binding.mainRecycleView.apply {
            toolKitAdaptor = ToolKitAndReviewAdaptor(requireActivity()) {
                activity?.toastMsg("$it")
            }
            adapter = toolKitAdaptor
            toolKitAdaptor.submitList(VideoForReview.getVideContent())
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            changeLayoutOnPortrait()
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changeLayoutLandScape()
        }
    }


    private fun changeLayoutOnPortrait() {

        val param = binding.mainVideoLayout.layoutParams
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

    }


    private fun changeLayoutLandScape() {

        val param = binding.mainVideoLayout.layoutParams
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

    }

}