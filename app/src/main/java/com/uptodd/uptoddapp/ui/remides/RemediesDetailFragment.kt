package com.uptodd.uptoddapp.ui.remides

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.RemediesDetailLayoutFragmentBinding

class RemediesDetailFragment :Fragment(R.layout.remedies_detail_layout_fragment) {

    private lateinit var binding:RemediesDetailLayoutFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=RemediesDetailLayoutFragmentBinding.bind(view)

    }

}