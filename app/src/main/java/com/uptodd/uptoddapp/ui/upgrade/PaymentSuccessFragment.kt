package com.uptodd.uptoddapp.ui.upgrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.FragmentPaymentSuccessBinding
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import java.text.SimpleDateFormat

class PaymentSuccessFragment :Fragment()
{
    var binding:FragmentPaymentSuccessBinding?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentPaymentSuccessBinding.inflate(inflater,container,false)
        UpgradeViewModel.paymentDone=true

        if(!AllUtil.isUserPremium(requireContext()))
        {
            val end= SimpleDateFormat("yyyy-MM-dd").parse(UptoddSharedPreferences.getInstance(requireContext()).getSubEnd())

            if(AllUtil.isSubscriptionOver(end))
            {
                binding?.logout?.visibility=View.VISIBLE
                binding?.logout?.setOnClickListener {
                    AllUtil.logout(requireContext(),requireActivity())
                }
            }
        }

        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar!!
     supportActionBar.title="Welcome"
        super.onCreate(savedInstanceState)
    }




}