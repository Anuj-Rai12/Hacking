package com.example.hackingwork.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.hackingwork.R
import com.example.hackingwork.databinding.AddUserCourseFragmentBinding

class AddUserCourse :Fragment(R.layout.add_user_course_fragment){
    private lateinit var binding:AddUserCourseFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= AddUserCourseFragmentBinding.bind(view)
    }
}