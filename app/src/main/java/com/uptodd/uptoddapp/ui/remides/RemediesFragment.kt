package com.uptodd.uptoddapp.ui.remides

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.RemidesLayoutFragmentsBinding
import com.uptodd.uptoddapp.ui.home.homePage.repo.HomPageRepository
import com.uptodd.uptoddapp.ui.remides.viewmodel.RemediesViewModel
import com.uptodd.uptoddapp.utilities.ToolbarUtils
import com.uptodd.uptoddapp.utils.toastMsg

class RemediesFragment : Fragment(R.layout.remides_layout_fragments) {

    private lateinit var binding: RemidesLayoutFragmentsBinding
    private val viewModel: RemediesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RemidesLayoutFragmentsBinding.bind(view)
        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.activity_remedies), "Symptoms and it's Cure",
            R.drawable.app_icon_image
        )

        viewModel.getRemedies()
        getRemedies()


    }

    private fun getRemedies() {
        viewModel.remediesResponse.observe(viewLifecycleOwner) {
            when (HomPageRepository.Companion.AndroidNetworkingResponseWrapper.valueOf(it.first)) {
                HomPageRepository.Companion.AndroidNetworkingResponseWrapper.SUCCESS -> {
                    activity?.toastMsg("${it.second}")
                }
                HomPageRepository.Companion.AndroidNetworkingResponseWrapper.ERROR -> {
                    activity?.toastMsg("${it.second}")
                }
                HomPageRepository.Companion.AndroidNetworkingResponseWrapper.LOADING -> {
                    activity?.toastMsg("${it.second}")
                }
            }
        }
    }
}