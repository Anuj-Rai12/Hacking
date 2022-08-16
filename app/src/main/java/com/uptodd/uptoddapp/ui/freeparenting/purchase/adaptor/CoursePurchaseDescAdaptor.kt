package com.uptodd.uptoddapp.ui.freeparenting.purchase.adaptor

import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uptodd.uptoddapp.databinding.DescriptionCourseTextLayoutBinding


class CoursePurchaseDescAdaptor :
    ListAdapter<String, CoursePurchaseDescAdaptor.CourseDescViewHolder>(diffUtil) {
    inner class CourseDescViewHolder(private val binding: DescriptionCourseTextLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(data: String) {
            binding.courseDescTxt.text = data
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(
                oldItem: String,
                newItem: String
            ) = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: String,
                newItem: String
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseDescViewHolder {
        val binding = DescriptionCourseTextLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseDescViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseDescViewHolder, position: Int) {
        val currItem = getItem(position)
        currItem?.let {
            holder.setData(it)
        }
    }

}