package com.uptodd.uptoddapp.ui.loginfreeorpaid.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.databinding.CourseInfoLayoutBinding

class CourseInfoFragment(private val title: String, private val img: Int) :
    Fragment(R.layout.course_info_layout) {

    private lateinit var binding: CourseInfoLayoutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding= CourseInfoLayoutBinding.bind(view)
        binding.txtOfCourseInfo.text=title
        binding.courseInfoImg.setImageResource(img)

    }
}