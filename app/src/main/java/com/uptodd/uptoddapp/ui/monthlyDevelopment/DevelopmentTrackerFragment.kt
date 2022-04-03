package com.uptodd.uptoddapp.ui.monthlyDevelopment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.DevelopmentTrackerFragmentBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.adapters.DevelopmentTrackerAdapter
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.AllResponse
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.ToolbarUtils

class DevelopmentTrackerFragment:Fragment(),DevelopmentTrackerAdapter.DevelopmentTrackerListener {

    var binding:DevelopmentTrackerFragmentBinding?= null
    var viewModel:DevelopmentTrackerViewModel?=null
    var adapter:DevelopmentTrackerAdapter?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DevelopmentTrackerFragmentBinding.inflate(inflater)
        viewModel = ViewModelProvider(this)[DevelopmentTrackerViewModel::class.java]
        ToolbarUtils.initToolbar(
            requireActivity(), binding?.collapseToolbar!!,
            findNavController(),"Monthly Development form","",
            R.drawable.poem_icon
        )


        if(AllUtil.isUserPremium(requireContext()))
        {
            if(!AllUtil.isSubscriptionOverActive(requireContext()))
            {
                binding?.upgradeButton?.visibility= View.GONE
            }
        }
        binding?.upgradeButton?.setOnClickListener {

            it.findNavController().navigate(R.id.action_developmentTrackerFragment_to_upgradeFragment)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DevelopmentTrackerAdapter()
        adapter?.listener=this
        binding?.trackerListRecyclerView?.adapter = adapter
        viewModel?.fetchTrackerResponse(requireContext())

        viewModel?.trackerList?.observe(viewLifecycleOwner) {

            if(it.data.allResponses.isEmpty()){
                binding?.noResponse?.visibility = View.VISIBLE
                binding?.trackerListRecyclerView?.visibility = View.GONE
            } else {
                binding?.noResponse?.visibility = View.GONE
                binding?.trackerListRecyclerView?.visibility = View.VISIBLE
                adapter?.add(it.data.allResponses)
            }
            binding?.trackerRefresh?.isRefreshing = false
            if(it.data.isTrackerFormOpen==1){
                binding?.fillForm?.visibility = View.VISIBLE
            }
        }
        viewModel?.errorResponse?.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context,"Error occur",Toast.LENGTH_LONG).show()
        })

        binding?.trackerRefresh?.setOnRefreshListener {
            viewModel?.fetchTrackerResponse(requireContext())
        }
    }

    override fun onClick(allResponse: AllResponse) {
        findNavController().navigate(DevelopmentTrackerFragmentDirections.actionDevelopmentTrackerFragmentToTrackerResponseFragment(
            allResponse.getAllQuestions(),allResponse.tips
        ))
    }

}