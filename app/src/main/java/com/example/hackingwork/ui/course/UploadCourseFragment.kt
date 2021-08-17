package com.example.hackingwork.ui.course

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.hackingwork.R
import com.example.hackingwork.databinding.UploadCourseLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UploadCourseFragment : Fragment(R.layout.upload_course_layout) {
    private lateinit var binding:UploadCourseLayoutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= UploadCourseLayoutBinding.bind(view)
    }
}