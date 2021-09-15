package com.example.hackerstudent.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.ExploreFragmentBinding
import com.example.hackerstudent.utils.hide

class ExploreFragment : Fragment(R.layout.explore_fragment) {
    private lateinit var binding: ExploreFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.hide()
        binding = ExploreFragmentBinding.bind(view)
    }
}