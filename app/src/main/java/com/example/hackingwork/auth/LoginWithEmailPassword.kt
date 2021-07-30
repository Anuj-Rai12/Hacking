package com.example.hackingwork.auth

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.databinding.LoginWithEmailPasswordBinding
import com.example.hackingwork.utils.ActivityDataInfo
import com.example.hackingwork.utils.InputDataBitch
import com.example.hackingwork.utils.getIntent


class LoginWithEmailPassword : Fragment(R.layout.login_with_email_password) {
    private lateinit var binding: LoginWithEmailPasswordBinding
    private val onActivityStart = registerForActivityResult(ActivityDataInfo()) {output->
        output.email?.let { email ->
            if (output.requestCode)
            binding.emailText.setText(email)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginWithEmailPasswordBinding.bind(view)
        try {
            onActivityStart.launch(InputDataBitch(getIntent()))
        }catch (e:ActivityNotFoundException){
            Log.i(TAG, "onViewCreated: Activity Not Found Exception")
        }
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