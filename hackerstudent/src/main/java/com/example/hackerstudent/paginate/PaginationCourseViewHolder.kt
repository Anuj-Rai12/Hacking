package com.example.hackerstudent.paginate

import androidx.recyclerview.widget.RecyclerView
import com.example.hackerstudent.databinding.CourseImagesBinding
import com.example.hackerstudent.utils.FireBaseCourseTitle

class PaginationCourseViewHolder(private val binding: CourseImagesBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bindIt(
        fireBaseCourseTitle: FireBaseCourseTitle,
        itemClicked: (FireBaseCourseTitle) -> Unit
    ) {
        binding.apply {
            root.setOnClickListener {
                itemClicked(fireBaseCourseTitle)
            }
        }
    }
}