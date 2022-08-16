package com.uptodd.uptoddapp.ui.freeparenting.content.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FreeDemoVideoModuleFragmentsBinding

class FreeDemoVideoModuleFragments :
    Fragment(R.layout.free_demo_video_module_fragments) {
    private lateinit var binding: FreeDemoVideoModuleFragmentsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FreeDemoVideoModuleFragmentsBinding.bind(view)

    }
}