package com.example.hackerstudent.recycle.paid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.FeatureCourseLayoutBinding
import com.example.hackerstudent.databinding.MyUserItemBinding
import com.example.hackerstudent.utils.PaidCourseSealed
import com.example.hackerstudent.utils.UploadFireBaseData


class AllPaidAdaptor constructor(
    private val item: (UploadFireBaseData) -> Unit
) :
    ListAdapter<PaidCourseSealed, AllPaidCourseViewHolder>(diff) {
    companion object {
        val diff = object : DiffUtil.ItemCallback<PaidCourseSealed>() {
            override fun areItemsTheSame(
                oldItem: PaidCourseSealed,
                newItem: PaidCourseSealed
            ) = getValue(oldItem) == getValue(newItem)

            private fun getValue(paidCourseSealed: PaidCourseSealed) {
                when (paidCourseSealed) {
                    is PaidCourseSealed.CourseList -> paidCourseSealed.title
                    is PaidCourseSealed.User -> paidCourseSealed.name
                }
            }

            override fun areContentsTheSame(
                oldItem: PaidCourseSealed,
                newItem: PaidCourseSealed
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPaidCourseViewHolder {
        return when (viewType) {
            R.layout.feature_course_layout -> {
                AllPaidCourseViewHolder.CourseFeatureLayout(
                    binding = FeatureCourseLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.my_user_item -> {
                AllPaidCourseViewHolder.MyUserItemViewHolder(
                    binding = MyUserItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("Layout Error Course")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PaidCourseSealed.CourseList -> R.layout.feature_course_layout
            is PaidCourseSealed.User -> R.layout.my_user_item
        }
    }

    override fun onBindViewHolder(holder: AllPaidCourseViewHolder, position: Int) {
        val curr = getItem(position)
        curr?.let {
            when (holder) {
                is AllPaidCourseViewHolder.CourseFeatureLayout -> holder.bindIt(
                    it as PaidCourseSealed.CourseList,
                    item
                )
                is AllPaidCourseViewHolder.MyUserItemViewHolder -> holder.bindIt(it as PaidCourseSealed.User)
            }
        }
    }
}