package com.example.hackingwork.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.databinding.CreateUserAccountBinding

class CreateUserAccount : Fragment(R.layout.create_user_account) {
    private lateinit var binding: CreateUserAccountBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreateUserAccountBinding.bind(view)
        binding.nextBtn.setOnClickListener {
            //OTP Screen
            val action=CreateUserAccountDirections.actionGlobalPhoneNumberOtp()
            findNavController().navigate(action)
        }
        binding.backTo.setOnClickListener {
            //Back to Set On click
            val action=CreateUserAccountDirections.actionCreateUserAccountToLoginWithEmailPassword()
            findNavController().navigate(action)
        }
    }
}