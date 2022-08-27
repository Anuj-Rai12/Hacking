package com.uptodd.uptoddapp.ui.remides

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.RemidesLayoutFragmentsBinding
import com.uptodd.uptoddapp.utilities.ToolbarUtils

class RemediesFragment : Fragment(R.layout.remides_layout_fragments) {

    private lateinit var binding: RemidesLayoutFragmentsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RemidesLayoutFragmentsBinding.bind(view)
        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.activity_remedies), "Symptoms and it's Cure",
            R.drawable.app_icon_image
        )
    }
}