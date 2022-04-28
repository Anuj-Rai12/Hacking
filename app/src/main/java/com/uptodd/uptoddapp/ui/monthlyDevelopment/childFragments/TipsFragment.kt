package com.uptodd.uptoddapp.ui.monthlyDevelopment.childFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.databinding.FragmentTipsBinding
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Response

class TipsFragment : Fragment() {

    private lateinit var binding: FragmentTipsBinding

    var response:String?=null

    companion object {
        fun getInstance(response: String?):TipsFragment{
            val fragment=TipsFragment()
            val bundle = Bundle()
            bundle.putString("tips",response)
            fragment.arguments=bundle

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTipsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        response = arguments?.getString("tips")

        if(response?.isEmpty()  == true || response.isNullOrBlank()){
            showNoData()
        } else {
            hideNodata()
            binding.tvTips.text = response
        }


    }
    private fun hideNodata() {
        binding.noDataContainer.visibility = View.GONE
    }

    private fun showNoData() {
        binding.noDataContainer.visibility=View.VISIBLE
    }
}