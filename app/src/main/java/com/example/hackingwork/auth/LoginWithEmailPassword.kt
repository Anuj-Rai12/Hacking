package com.example.hackingwork.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.databinding.LoginWithEmailPasswordBinding

class LoginWithEmailPassword : Fragment(R.layout.login_with_email_password) {
    private lateinit var binding: LoginWithEmailPasswordBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginWithEmailPasswordBinding.bind(view)
        binding.backSign.setOnClickListener {
            //Create Acc
            val action=LoginWithEmailPasswordDirections.actionLoginWithEmailPasswordToCreateUserAccount()
            findNavController().navigate(action)
        }
        binding.forpass.setOnClickListener {
            //Forget Password
            val action=LoginWithEmailPasswordDirections.actionLoginWithEmailPasswordToForgetFragment()
            findNavController().navigate(action)
        }
        binding.nextBtn.setOnClickListener {
            //New Screen
            val action=LoginWithEmailPasswordDirections.actionLoginWithEmailPasswordToAdminActivity()
            findNavController().navigate(action)
            activity?.finish()
        }
    }

}