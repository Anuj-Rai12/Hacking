package com.example.hackerstudent.recycle

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.hackerstudent.databinding.FeatureCourseLayoutBinding
import com.example.hackerstudent.databinding.LayoutImageCourseBinding
import com.example.hackerstudent.databinding.TitleQouteFramgentBinding
import com.example.hackerstudent.utils.CourseSealed
import com.example.hackerstudent.utils.UploadFireBaseData

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

    class CourseFeatureLayout(private val binding: FeatureCourseLayoutBinding) :
        AllViewHolder(binding) {
        private lateinit var recycleAdaptor: CourseRecycleAdaptor
        fun bindIt(course: CourseSealed.Course, item: (UploadFireBaseData) -> Unit) {
            binding.featureTitle.text = course.title
            binding.courseRecycle.apply {
                setHasFixedSize(true)
                recycleAdaptor = CourseRecycleAdaptor {
                    item(it)
                }
                adapter = recycleAdaptor
            }
            recycleAdaptor.submitList(course.fireBaseCourseTitle)
        }
    }
}