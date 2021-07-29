package com.example.hackingwork.auth.forgetpass

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.databinding.MkeFramgentBinding

class MakeSelection :Fragment(R.layout.mke_framgent){
    private lateinit var binding: MkeFramgentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= MkeFramgentBinding.bind(view)
        binding.emailOption.setOnClickListener {
            val  action=MakeSelectionDirections.actionGlobalPhoneNumberOtp()
            findNavController().navigate(action)
        }
        binding.phoneOption.setOnClickListener {
            val  action=MakeSelectionDirections.actionGlobalPhoneNumberOtp()
            findNavController().navigate(action)
        }
    }

}