package com.example.hackerstudent.paginate

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.hackerstudent.databinding.CourseItemLayoutBinding
import com.example.hackerstudent.utils.UploadFireBaseData

class PaginationAdaptor(private val itemClicked: (UploadFireBaseData) -> Unit,private val context: Context) :
    PagingDataAdapter<UploadFireBaseData, ExploreCourseViewHolder>(courseDiff) {
    override fun onBindViewHolder(holder: ExploreCourseViewHolder, position: Int) {
        val current = getItem(position)
        current?.let {
            holder.bindIt(it, itemClicked)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreCourseViewHolder {
        val binding =
            CourseItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExploreCourseViewHolder(binding,context)
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