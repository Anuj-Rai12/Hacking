package com.example.hackerstudent.recycle.preview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.*
import com.example.hackerstudent.utils.CoursePreview

class AllPreviewAdaptor(
    private val itemBuy: (String, String) -> Unit,
    private val itemCart: (String, String) -> Unit,
    private val teacherLike: (String) -> Unit,
    private val itemMoreReview: (String) -> Unit,
    private val viewClick: (String) -> Unit
) :
    ListAdapter<CoursePreview, AllPreviewViewHolder>(diffUtil) {


    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CoursePreview>() {
            override fun areItemsTheSame(oldItem: CoursePreview, newItem: CoursePreview): Boolean {
                return getValue(oldItem) == getValue(newItem)
            }

            private fun getValue(coursePreview: CoursePreview): String? {
                return when (coursePreview) {
                    is CoursePreview.ArrayClass -> coursePreview.title
                    is CoursePreview.CoursePrice -> coursePreview.title
                    is CoursePreview.CourseRatingAndOther -> coursePreview.rating
                    is CoursePreview.ReviewSection -> coursePreview.data.bywhom
                    is CoursePreview.VideoCourse -> coursePreview.title
                }
            }

            override fun areContentsTheSame(
                oldItem: CoursePreview,
                newItem: CoursePreview
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPreviewViewHolder {
        return when (viewType) {
            R.layout.feature_course_layout -> {
                AllPreviewViewHolder.FeatureClassLikeRequirement(
                    binding = FeatureCourseLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.course_purchase_layout -> {
                AllPreviewViewHolder.CoursePurchaseData(
                    binding = CoursePurchaseLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.title_qoute_framgent -> {
                AllPreviewViewHolder.CourseRatingAndOther(
                    binding = TitleQouteFramgentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.review_section_layout -> {
                AllPreviewViewHolder.UserReviewTitle(
                    binding = ReviewSectionLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.layout_image_course -> {
                AllPreviewViewHolder.VideoTitle(
                    binding = LayoutImageCourseBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("No Preview Layout Found")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CoursePreview.ArrayClass -> R.layout.feature_course_layout
            is CoursePreview.CoursePrice -> R.layout.course_purchase_layout
            is CoursePreview.CourseRatingAndOther -> R.layout.title_qoute_framgent
            is CoursePreview.ReviewSection -> R.layout.review_section_layout
            is CoursePreview.VideoCourse -> R.layout.layout_image_course
        }
    }

    override fun onBindViewHolder(holder: AllPreviewViewHolder, position: Int) {
        val current = getItem(position)
        current?.let { currentItem ->
            when (holder) {
                is AllPreviewViewHolder.CoursePurchaseData -> holder.bindIt(
                    amount = currentItem as CoursePreview.CoursePrice,
                    itemBuy,
                    itemCart
                )
                is AllPreviewViewHolder.CourseRatingAndOther -> holder.bindIt(
                    currentItem as CoursePreview.CourseRatingAndOther,
                    teacherLike = teacherLike
                )
                is AllPreviewViewHolder.FeatureClassLikeRequirement -> holder.binIt(currentItem as CoursePreview.ArrayClass)
                is AllPreviewViewHolder.UserReviewTitle -> holder.bindIt(
                    currentItem as CoursePreview.ReviewSection,
                    itemMoreReview = itemMoreReview
                )
                is AllPreviewViewHolder.VideoTitle -> holder.bindIt(
                    currentItem as CoursePreview.VideoCourse,
                    viewClick = viewClick
                )
            }
        }
    }
}