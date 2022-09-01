package com.uptodd.uptoddapp.ui.remides

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.RemidesLayoutFragmentsBinding
import com.uptodd.uptoddapp.ui.home.homePage.repo.HomPageRepository
import com.uptodd.uptoddapp.ui.remides.adaptor.RemediesAdaptor
import com.uptodd.uptoddapp.ui.remides.adaptor.RemediesInterface
import com.uptodd.uptoddapp.ui.remides.model.Disease
import com.uptodd.uptoddapp.ui.remides.model.RemediesResponse
import com.uptodd.uptoddapp.ui.remides.viewmodel.RemediesViewModel
import com.uptodd.uptoddapp.utilities.ToolbarUtils
import com.uptodd.uptoddapp.utils.hide
import com.uptodd.uptoddapp.utils.setUpErrorMessageDialog

class RemediesFragment : Fragment(R.layout.remides_layout_fragments), RemediesInterface {

    private lateinit var binding: RemidesLayoutFragmentsBinding
    private val viewModel: RemediesViewModel by viewModels()
    private val remediesAdaptor = RemediesAdaptor()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RemidesLayoutFragmentsBinding.bind(view)
        ToolbarUtils.initToolbar(
            requireActivity(), binding.collapseToolbar,
            findNavController(), getString(R.string.activity_remedies), "Symptoms and it's Cure",
            R.drawable.ic_gradma_tip
        )

        viewModel.getRemedies()
        setRecycle()
        getRemedies()
        binding.swipeLayout.setOnRefreshListener {
            viewModel.getRemedies()
        }

    }

    private fun setRecycle() {
        binding.reminderRecycleView.apply {
            remediesAdaptor.listener = this@RemediesFragment
            adapter = remediesAdaptor
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getRemedies() {
        viewModel.remediesResponse.observe(viewLifecycleOwner) {
            when (HomPageRepository.Companion.AndroidNetworkingResponseWrapper.valueOf(it.first)) {
                HomPageRepository.Companion.AndroidNetworkingResponseWrapper.SUCCESS -> {
                    hide()
                    (it.second as RemediesResponse?)?.let { remedies ->
                        if (remedies.data.isEmpty()) {
                            binding.pbTxt.text = "No Data Found"
                        } else {
                            remediesAdaptor.submitList(remedies.data)
                        }
                    }
                }
                HomPageRepository.Companion.AndroidNetworkingResponseWrapper.ERROR -> {
                    hide()
                    setUpErrorMessageDialog(
                        "${it.second}",
                        "Cannot process the Remedies Request ,so please Try Again.."
                    )
                }
                HomPageRepository.Companion.AndroidNetworkingResponseWrapper.LOADING -> {
                    showPb()
                    binding.pbTxt.text = "${it.second}"
                }
            }
        }
    }

    private fun showPb() {
        binding.pbLoading.show()
        binding.swipeLayout.isRefreshing = true
    }

    private fun hide() {
        binding.pbLoading.hide()
        binding.pbTxt.hide()
        binding.swipeLayout.isRefreshing = false
    }

    override fun getRemedies(response: Disease) {
        val action =
            RemediesFragmentDirections.actionRemediesFragmentToRemediesDetailFragment(response)
        findNavController().navigate(action)
    }

}