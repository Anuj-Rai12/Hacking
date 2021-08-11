package com.example.hackerstudent.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.ProfileFramgnetBinding

class ProfileFragment : Fragment(R.layout.profile_framgnet) {
    private lateinit var binding: ProfileFramgnetBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProfileFramgnetBinding.bind(view)
    }
}