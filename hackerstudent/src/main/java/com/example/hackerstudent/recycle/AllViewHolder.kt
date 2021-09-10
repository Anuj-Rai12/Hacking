package com.example.hackerstudent.recycle

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.hackerstudent.databinding.FeatureCourseLayoutBinding
import com.example.hackerstudent.databinding.LayoutImageCourseBinding
import com.example.hackerstudent.databinding.TitleQouteFramgentBinding
import com.example.hackerstudent.utils.CourseSealed

sealed class AllViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class ImageViewHolder(private val binding: LayoutImageCourseBinding) : AllViewHolder(binding) {
        fun bindIt(image: CourseSealed.Image) {
            binding.apply {
                lottiFile.setAnimation(image.raw)
            }
        }
    }

    class CourseQuoteHolder(private val binding: TitleQouteFramgentBinding) :
        AllViewHolder(binding) {
        fun bindIt(title: CourseSealed.Title) {
            binding.apply {
                title.motivation?.let {
                    writerTitle.text = it.first().a
                    quoteTitle.text = it.first().q
                }
            }
        }
    }

    class CourseFeatureLayout(
        private val binding: FeatureCourseLayoutBinding,
        private val context: Context,
    ) :
        AllViewHolder(binding) {
        fun bindIt(course: CourseSealed.Course) {
            binding.featureTitle.text = course.title
            //binding
        }
    }
}