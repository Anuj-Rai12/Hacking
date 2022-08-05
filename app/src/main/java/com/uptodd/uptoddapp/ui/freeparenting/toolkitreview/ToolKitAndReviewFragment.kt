package com.uptodd.uptoddapp.ui.freeparenting.toolkitreview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.TookKitAndReviewLayoutBinding

class ToolKitAndReviewFragment : Fragment(R.layout.took_kit_and_review_layout) {
    private lateinit var binding: TookKitAndReviewLayoutBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = TookKitAndReviewLayoutBinding.bind(view)
    }


}