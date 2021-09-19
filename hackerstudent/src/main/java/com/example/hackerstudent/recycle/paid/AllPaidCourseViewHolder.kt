package com.example.hackerstudent.recycle.paid

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.hackerstudent.databinding.FeatureCourseLayoutBinding
import com.example.hackerstudent.databinding.MyUserItemBinding
import com.example.hackerstudent.recycle.CourseRecycleAdaptor
import com.example.hackerstudent.utils.PaidCourseSealed
import com.example.hackerstudent.utils.UploadFireBaseData

sealed class AllPaidCourseViewHolder(viewBinding: ViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {

    class MyUserItemViewHolder(private val binding: MyUserItemBinding) :
        AllPaidCourseViewHolder(binding) {
        @SuppressLint("SetTextI18n")
        fun bindIt(course: PaidCourseSealed.User) {
            binding.apply {
                simpleLottieFile.apply {
                    setAnimation(course.layout)
                    repeatCount = 1
                    setOnClickListener {
                        playAnimation()
                    }
                }
                userNameCourse.text = "Hi ${course.name},"
            }
        }
    }


    class CourseFeatureLayout(private val binding: FeatureCourseLayoutBinding) :
        AllPaidCourseViewHolder(binding) {
        private lateinit var recycleAdaptor: CourseRecycleAdaptor
        fun bindIt(course: PaidCourseSealed.CourseList, item: (UploadFireBaseData) -> Unit) {
            binding.featureTitle.text = course.title
            binding.courseRecycle.apply {
                setHasFixedSize(true)
                recycleAdaptor = CourseRecycleAdaptor {
                    item(it)
                }
                adapter = recycleAdaptor
            }
            recycleAdaptor.submitList(course.uploadFireBaseData)
        }
    }


}