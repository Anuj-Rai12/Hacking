package com.example.hackingwork.auth.forgetpass

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.databinding.ForgetFragmentBinding

class ForgetFragment :Fragment(R.layout.forget_fragment){
private lateinit var binding: ForgetFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= ForgetFragmentBinding.bind(view)
        binding.nextBtn.setOnClickListener {
            val action=ForgetFragmentDirections.actionForgetFragmentToMakeSelection()
            findNavController().navigate(action)
        }

        binding.createacc.setOnClickListener {
            val action=ForgetFragmentDirections.actionForgetFragmentToCreateUserAccount()
            findNavController().navigate(action)
        }
    }
}