package com.example.hackerstudent.recycle.preview

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.load
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.*
import com.example.hackerstudent.utils.*

sealed class AllPreviewViewHolder(viewBinding: ViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {

    class VideoTitle(private val binding: LayoutImageCourseBinding) :
        AllPreviewViewHolder(binding) {
        fun bindIt(video: CoursePreview.VideoCourse, viewClick: (String) -> Unit) {
            binding.apply {
                lottiFile.hide()

                videoPreview.show()
                titleDescribeTitle.show()
                playTxt.show()

                videoPreview.load(video.thumbnail) {
                    crossfade(true)
                    placeholder(R.drawable.book_icon)
                }
                root.setOnClickListener {
                    viewClick(video.videoPreview)
                }
                titleDescribeTitle.text = video.title
            }
        }
    }

    class CourseRatingAndOther(private val binding: TitleQouteFramgentBinding) :
        AllPreviewViewHolder(binding) {
        @SuppressLint("SetTextI18n")
        fun bindIt(rating: CoursePreview.CourseRatingAndOther, teacherLike: (String) -> Unit) {
            binding.apply {
                headingTitle.hide()
                writerTitle.hide()
                quoteTitle.hide()

                courseRating.show()
                courseByTxt.show()
                teacherNameTxt.show()
                langTxt.show()
                avgRating.show()
                totalHrsTxt.show()

                totalHrsTxt.text = "${rating.totalHrs} hrs"
                avgRating.text = rating.rating
                courseRating.rating = rating.rating.toFloat()
                teacherNameTxt.setOnClickListener {
                    teacherLike(teacherNameTxt.text.toString())
                }
            }
        }
    }


    class FeatureClassLikeRequirement(
        private val binding: FeatureCourseLayoutBinding,
        private val context: FragmentActivity
    ) :
        AllPreviewViewHolder(binding) {
        private val list: MutableList<RequirementData> = mutableListOf()
        private val requirementAdaptor by lazy {
            RequirementAdaptor()
        }

        fun binIt(data: CoursePreview.ArrayClass) {
            binding.apply {
                featureTitle.hide()
                courseRecycle.hide()

                recycleTitleText.show()
                reqLayoutRecycle.show()

                recycleTitleText.text = data.title
                if (data.requirement != null) {
                    data.requirement.forEach {
                        list.add(RequirementData(it))
                    }
                } else if (data.targetAudience != null) {
                    data.targetAudience.forEach {
                        list.add(RequirementData(it))
                    }
                }
                reqLayoutRecycle.apply {
                    setHasFixedSize(true)
                    layoutManager =
                        WrapContentLinearLayoutManager(this@FeatureClassLikeRequirement.context)
                    adapter = requirementAdaptor
                }
                requirementAdaptor.submitList(list)
            }
        }
    }

    class CoursePurchaseData(private val binding: CoursePurchaseLayoutBinding) :
        AllPreviewViewHolder(binding) {
        @SuppressLint("SetTextI18n")
        fun bindIt(
            amount: CoursePreview.CoursePrice,
            itemBuy: (String, String) -> Unit,
            itemCart: (String, String) -> Unit
        ) {

            binding.apply {
                val current = amount.currAmt.toInt()
                val mrp = amount.mrp.toInt()
                addToCart.setOnClickListener {
                    itemCart(amount.title, amount.currAmt)
                }
                courseBuyBtn.setOnClickListener {
                    itemBuy(amount.title, amount.currAmt)
                }
                courseCurrentPriceTxt.text = "${GetConstStringObj.Rs} $current"
                courseMrpCurrentPriceTxt.text = "${GetConstStringObj.Rs} $mrp"
                courseMrpCurrentPriceTxt.paint.isStrikeThruText = true
                courseDiscountPriceTxt.text =
                    "${getDiscount(currPrice = current.toDouble(), mrpPrice = mrp.toDouble())}%"
            }
        }
    }

    class UserReviewTitle(private val binding: ReviewSectionLayoutBinding) :
        AllPreviewViewHolder(binding) {
        fun bindIt(review: CoursePreview.ReviewSection, itemMoreReview: (String) -> Unit) {
            binding.apply {
                courseReviewDescTxt.text = review.data.description
                ratingReviewBar.rating = review.data.rateing?.toFloat() ?: (4.5).toFloat()
                userNameTxt.text = review.data.bywhom
                moreReviewBtn.setOnClickListener {
                    itemMoreReview(review.data.bywhom ?: "more review")
                }
            }
        }
    }
}