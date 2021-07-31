package com.example.hackingwork.auth


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.MainActivity
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.databinding.PhoneOtpFaragmentBinding
import com.example.hackingwork.viewmodels.PrimaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneNumberOtp :Fragment(R.layout.phone_otp_faragment){
    private lateinit var binding:PhoneOtpFaragmentBinding
    private val primaryViewModel:PrimaryViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= PhoneOtpFaragmentBinding.bind(view)
        Log.i(TAG, "onViewCreated: ${MainActivity.emailAuthLink}")
        primaryViewModel.read.observe(viewLifecycleOwner){
            binding.phoneno.text=it.phone
        }
        binding.verify.setOnClickListener {
            val action=PhoneNumberOtpDirections.actionPhoneNumberOtpToAdminActivity()
            findNavController().navigate(action)
            activity?.finish()
        }
    }

}