package com.example.hackerstudent.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.api.Motivation
import com.example.hackerstudent.databinding.HomeScreenFramgmentBinding
import com.example.hackerstudent.utils.MySealed
import com.example.hackerstudent.viewmodels.CourseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : Fragment(R.layout.home_screen_framgment) {
    private lateinit var binding: HomeScreenFramgmentBinding
    private val courseViewModel: CourseViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = HomeScreenFramgmentBinding.bind(view)
        courseViewModel.getTodayQuote.observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> Log.i(TAG, "onViewCreated: ${it.exception?.localizedMessage}")
                is MySealed.Loading -> Log.i(TAG, "onViewCreated:${it.data}")
                is MySealed.Success -> {
                    val data =it.data as Motivation
                    Log.i(TAG, "onViewCreated: $data")
                }
            }
        }
    }
}