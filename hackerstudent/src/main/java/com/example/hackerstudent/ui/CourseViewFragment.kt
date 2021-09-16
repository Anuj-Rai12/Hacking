package com.example.hackerstudent.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.CourseViewFragmentBinding
import com.example.hackerstudent.utils.hide
import com.example.hackerstudent.utils.hideBottomNavBar

class CourseViewFragment :Fragment(R.layout.course_view_fragment){
    private lateinit var binding:CourseViewFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).hide()
        hideBottomNavBar()
        binding= CourseViewFragmentBinding.bind(view)
    }
}