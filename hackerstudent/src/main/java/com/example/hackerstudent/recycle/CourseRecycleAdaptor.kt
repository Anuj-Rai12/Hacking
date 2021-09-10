package com.example.hackerstudent.recycle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.hackerstudent.databinding.CourseImagesBinding
import com.example.hackerstudent.paginate.PaginationAdaptor
import com.example.hackerstudent.paginate.PaginationCourseViewHolder
import com.example.hackerstudent.utils.UploadFireBaseData

class CourseRecycleAdaptor(private val item: (UploadFireBaseData) -> Unit) :
    ListAdapter<UploadFireBaseData, PaginationCourseViewHolder>(PaginationAdaptor.courseDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaginationCourseViewHolder {
        val binding =
            CourseImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaginationCourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaginationCourseViewHolder, position: Int) {
        val curr = getItem(position)
        curr?.let {
            holder.bindIt(it, item)
        }
    }
}