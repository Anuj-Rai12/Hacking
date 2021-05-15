package com.uptodd.uptoddapp.doctor.resources

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.ResourcesFragmentBinding

class ResourcesFragment : Fragment() {

    companion object {
        fun newInstance() = ResourcesFragment()
    }

    private lateinit var viewModel: ResourcesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {


        val binding: ResourcesFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.resources_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(ResourcesViewModel::class.java)
        binding.resourcesFragmentBinding = viewModel

        setClickListeners(binding)

        return binding.root
    }

    private fun setClickListeners(binding: ResourcesFragmentBinding) {
        binding.resourcesDoctorBrochure.setOnClickListener {
            findNavController().navigate(
                ResourcesFragmentDirections.actionResourcesFragmentToViewPDFFragment(
                    "http://uptodd.com/resources/doctor/DoctorBrochure.pdf",
                    "Brochure"
                )
            )
        }
        binding.resourcesDoctorAppGuide.setOnClickListener {
            findNavController().navigate(
                ResourcesFragmentDirections.actionResourcesFragmentToViewPDFFragment(
                    "http://uptodd.com/resources/doctor/DoctorAppGuide.pdf",
                    "App Guide"
                )
            )
        }
        binding.resourcesDoctorReferralGuide.setOnClickListener {
            findNavController().navigate(
                ResourcesFragmentDirections.actionResourcesFragmentToViewPDFFragment(
                    "http://uptodd.com/resources/doctor/DoctorReferralGuide.pdf",
                    "ReferralGuide"
                )
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ResourcesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}