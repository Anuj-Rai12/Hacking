package com.example.hackerstudent.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.HomeScreenFramgmentBinding

class HomeScreenFragment : Fragment(R.layout.home_screen_framgment) {
    private lateinit var binding: HomeScreenFramgmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = HomeScreenFramgmentBinding.bind(view)
    }
}