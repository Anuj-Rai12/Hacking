package com.example.hackerstudent.paginate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.hackerstudent.databinding.CourseImagesBinding
import com.example.hackerstudent.utils.FireBaseCourseTitle

class PaginationAdaptor(private val itemClicked: (FireBaseCourseTitle) -> Unit) :
    PagingDataAdapter<FireBaseCourseTitle, PaginationCourseViewHolder>(courseDiff) {
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
        val courseDiff = object : DiffUtil.ItemCallback<FireBaseCourseTitle>() {
            override fun areItemsTheSame(
                oldItem: FireBaseCourseTitle,
                newItem: FireBaseCourseTitle
            ): Boolean {
                return oldItem.lastdate == newItem.lastdate
            }

            override fun areContentsTheSame(
                oldItem: FireBaseCourseTitle,
                newItem: FireBaseCourseTitle
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}