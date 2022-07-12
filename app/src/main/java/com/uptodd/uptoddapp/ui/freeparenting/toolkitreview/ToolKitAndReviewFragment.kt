package com.uptodd.uptoddapp.ui.freeparenting.toolkitreview

import android.os.Bundle
import android.view.View
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


    /*override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
    }


    private fun changeLayoutLandScape() {



    }*/

}