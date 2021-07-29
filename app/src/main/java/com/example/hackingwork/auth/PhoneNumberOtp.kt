package com.example.hackingwork.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.databinding.PhoneOtpFaragmentBinding

class PhoneNumberOtp :Fragment(R.layout.phone_otp_faragment){
    private lateinit var binding:PhoneOtpFaragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= PhoneOtpFaragmentBinding.bind(view)
        binding.verify.setOnClickListener {
            val action=PhoneNumberOtpDirections.actionPhoneNumberOtpToAdminActivity()
            findNavController().navigate(action)
            activity?.finish()
        }
    }

}