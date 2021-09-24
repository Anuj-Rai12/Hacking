package com.example.hackerstudent.recycle.addcart

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.hackerstudent.databinding.CourseItemLayoutBinding
import com.example.hackerstudent.paginate.ExploreCourseViewHolder
import com.example.hackerstudent.paginate.PaginationAdaptor
import com.example.hackerstudent.utils.UploadFireBaseData

class AddCartAdaptor(
    private val context: Context,
    private val itemClicked: (UploadFireBaseData) -> Unit
) : ListAdapter<UploadFireBaseData, ExploreCourseViewHolder>(PaginationAdaptor.courseDiff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreCourseViewHolder {
        val binding =
            CourseItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExploreCourseViewHolder(binding, context)
    }
    override fun onBindViewHolder(holder: ExploreCourseViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bindIt(it, itemClicked)
        }
    }
}