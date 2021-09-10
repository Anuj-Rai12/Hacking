package com.example.hackerstudent.paginate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.hackerstudent.databinding.CourseImagesBinding
import com.example.hackerstudent.utils.UploadFireBaseData

class PaginationAdaptor(private val itemClicked: (UploadFireBaseData) -> Unit) :
    PagingDataAdapter<UploadFireBaseData, PaginationCourseViewHolder>(courseDiff) {
    override fun onBindViewHolder(holder: PaginationCourseViewHolder, position: Int) {
        val current = getItem(position)
        current?.let {
            holder.bindIt(it, itemClicked)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaginationCourseViewHolder {
        val binding =
            CourseImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaginationCourseViewHolder(binding)
    }

    companion object {
        val courseDiff = object : DiffUtil.ItemCallback<UploadFireBaseData>() {
            override fun areItemsTheSame(
                oldItem: UploadFireBaseData,
                newItem: UploadFireBaseData
            ) = oldItem.fireBaseCourseTitle?.coursename == newItem.fireBaseCourseTitle?.coursename

            override fun areContentsTheSame(
                oldItem: UploadFireBaseData,
                newItem: UploadFireBaseData
            ) = oldItem == newItem
        }
    }
}