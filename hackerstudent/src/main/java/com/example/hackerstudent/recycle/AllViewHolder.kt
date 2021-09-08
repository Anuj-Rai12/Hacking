package com.example.hackerstudent.recycle

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.hackerstudent.databinding.FeatureCourseLayoutBinding
import com.example.hackerstudent.databinding.LayoutImageCourseBinding
import com.example.hackerstudent.databinding.TitleQouteFramgentBinding
import com.example.hackerstudent.utils.CourseSealed
import kotlinx.coroutines.launch

sealed class AllViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    class ImageViewHolder(private val binding: LayoutImageCourseBinding) : AllViewHolder(binding) {
        fun bindIt(image: CourseSealed.Image) {
            binding.apply {
                lottiFile.setAnimation(image.raw)
            }
        }
    }

    class CourseFeatureLayout(private val binding: TitleQouteFramgentBinding) :
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

    class CourseQuoteHolder(
        private val binding: FeatureCourseLayoutBinding,
        private val context: Context,
        private val lifecycleCoroutineScope: LifecycleCoroutineScope
    ) :
        AllViewHolder(binding) {
        fun bindIt(course: CourseSealed.Course) {
            binding.featureTitle.text = course.title
            lifecycleCoroutineScope.launch {

            }
        }
    }
}