package com.example.hackerstudent.recycle

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.FeatureCourseLayoutBinding
import com.example.hackerstudent.databinding.LayoutImageCourseBinding
import com.example.hackerstudent.databinding.TitleQouteFramgentBinding
import com.example.hackerstudent.utils.CourseSealed


class HomeAdaptorView(private val context: Context) :
    ListAdapter<CourseSealed, AllViewHolder>(diff) {
    companion object {
        val diff = object : DiffUtil.ItemCallback<CourseSealed>() {
            override fun areItemsTheSame(oldItem: CourseSealed, newItem: CourseSealed): Boolean {
                return getValue(oldItem) == getValue(newItem)
            }

            override fun areContentsTheSame(oldItem: CourseSealed, newItem: CourseSealed): Boolean {
                return oldItem == newItem
            }

        }

        private fun getValue(item: CourseSealed): String? {
            return when (item) {
                is CourseSealed.Course -> item.title
                is CourseSealed.Image -> item.Id
                is CourseSealed.Title -> item.title
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CourseSealed.Course -> R.layout.feature_course_layout
            is CourseSealed.Image -> R.layout.layout_image_course
            is CourseSealed.Title -> R.layout.title_qoute_framgent
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllViewHolder {
        return when (viewType) {
            R.layout.title_qoute_framgent -> {
                AllViewHolder.CourseQuoteHolder(
                    binding = TitleQouteFramgentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.feature_course_layout -> {
                AllViewHolder.CourseFeatureLayout(
                    binding = FeatureCourseLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    context = context
                )
            }
            R.layout.layout_image_course -> {
                AllViewHolder.ImageViewHolder(
                    binding = LayoutImageCourseBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw  IllegalArgumentException("No Activity Found")
        }
    }

    override fun onBindViewHolder(holder: AllViewHolder, position: Int) {
        val currentItem=getItem(position)
        currentItem?.let {
            when(holder){
                is AllViewHolder.CourseFeatureLayout ->  holder.bindIt(currentItem as CourseSealed.Course)
                is AllViewHolder.CourseQuoteHolder -> holder.bindIt(currentItem as CourseSealed.Title)
                is AllViewHolder.ImageViewHolder -> holder.bindIt(currentItem as CourseSealed.Image)
            }
        }
    }
}