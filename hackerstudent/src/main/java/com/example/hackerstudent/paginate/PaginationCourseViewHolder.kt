package com.example.hackerstudent.paginate

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.CourseImagesBinding
import com.example.hackerstudent.databinding.CourseItemLayoutBinding
import com.example.hackerstudent.utils.GetConstStringObj
import com.example.hackerstudent.utils.UploadFireBaseData
import com.example.hackerstudent.utils.getDiscount

class PaginationCourseViewHolder(private val binding: CourseImagesBinding) :
    RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bindIt(
        uploadFireBaseData: UploadFireBaseData,
        itemClicked: (UploadFireBaseData) -> Unit
    ) {
        binding.apply {
            val current = uploadFireBaseData.fireBaseCourseTitle?.currentprice?.toInt() ?: 1001
            val mrp = uploadFireBaseData.fireBaseCourseTitle?.totalprice?.toInt() ?: 10101
            CourseThumbnail.load(uploadFireBaseData.thumbnail) {
                crossfade(true)
                placeholder(R.drawable.book_icon)
            }
            courseTitleFile.text = uploadFireBaseData.fireBaseCourseTitle?.coursename
            roundedSenderTextView.text = "T"
            ratingBarStyle.rating =
                uploadFireBaseData.fireBaseCourseTitle?.review?.rateing?.toFloat()
                    ?: (4.5).toFloat()
            currentPrice.text = "${GetConstStringObj.Rs} $current"
            MRPPrice.text = "${GetConstStringObj.Rs} $mrp"
            discountPrice.text =
                "${getDiscount(currPrice = current.toDouble(), mrpPrice = mrp.toDouble())}%"
            MRPPrice.paint.isStrikeThruText = true
            root.setOnClickListener {
                itemClicked(uploadFireBaseData)
            }
        }
    }
}


class ExploreCourseViewHolder(private val binding: CourseItemLayoutBinding) :
    RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bindIt(uploadFireBaseData: UploadFireBaseData, itemClicked: (UploadFireBaseData) -> Unit) {
        binding.apply {
            courseImage.load(uploadFireBaseData.thumbnail) {
                crossfade(true)
                placeholder(R.drawable.book_icon)
            }
            courseTitleTxt.text = uploadFireBaseData.fireBaseCourseTitle?.coursename
            ratingBar.rating = uploadFireBaseData.fireBaseCourseTitle?.review?.rateing?.toFloat()
                ?: (4.5).toFloat()

            currentPriceTxt.text =
                "${GetConstStringObj.Rs} ${uploadFireBaseData.fireBaseCourseTitle?.currentprice}"

            root.setOnClickListener {
                itemClicked(uploadFireBaseData)
            }
        }
    }
}