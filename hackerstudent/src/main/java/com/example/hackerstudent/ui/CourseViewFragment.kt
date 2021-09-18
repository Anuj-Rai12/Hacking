package com.example.hackerstudent.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hackerstudent.R
import com.example.hackerstudent.databinding.CourseViewFragmentBinding
import com.example.hackerstudent.recycle.preview.AllPreviewAdaptor
import com.example.hackerstudent.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CourseViewFragment : Fragment(R.layout.course_view_fragment) {
    private lateinit var binding: CourseViewFragmentBinding
    private val list: MutableList<CoursePreview> = mutableListOf()
    private var allPreviewAdaptor: AllPreviewAdaptor? = null
    private val args: CourseViewFragmentArgs by navArgs()

    @Inject
    lateinit var networkUtils: NetworkUtils

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).hide()
        hideBottomNavBar()
        activity?.changeStatusBarColor()
        binding = CourseViewFragmentBinding.bind(view)
        binding.arrowImg.setOnClickListener {
            findNavController().popBackStack()
        }
        setUpRecycle()
        if (networkUtils.isConnected()) {
            getData()
        } else {
            hideOffline()
            requireActivity().msg(
                GetConstStringObj.NO_INTERNET,
                setAction = GetConstStringObj.RETRY, {
                    if (networkUtils.isConnected()) {
                        internetConnected()
                        getData()
                    }
                })
        }
        allPreviewAdaptor?.submitList(list)
        binding.shareImg.setOnClickListener {
            activity?.msg("Share Btn")
        }
    }

    private fun setUpRecycle() {
        binding.courseViewRecycle.apply {
            setHasFixedSize(true)
            allPreviewAdaptor = AllPreviewAdaptor({ courseName, CoursePrice ->
                //Item Buy
                context?.msg("CourseName Clicked -> $courseName, CoursePrice is -> $CoursePrice\n Item Purchased")
            }, { courseName, CoursePrice ->
                //Item Cart
                context?.msg("CourseName Clicked -> $courseName, CoursePrice is -> $CoursePrice\n Saved To Cart")
            }, { teacher ->
                context?.msg("Teacher -> $teacher")
            }, { goToMoreReview ->
                context?.msg("More Review Got Clicked $goToMoreReview")
            }, { video ->
                val action = CourseViewFragmentDirections.actionGlobalVideoFragment(
                    video,
                    "${args.data.coursename} Preview"
                )
                findNavController().navigate(action)
            })
            adapter = allPreviewAdaptor
        }
    }

    private fun internetConnected() {
        binding.courseViewRecycle.show()
        binding.errorLottieFile.hide()
    }

    private fun hideOffline() {
        binding.courseViewRecycle.hide()
        binding.errorLottieFile.show()
        binding.errorLottieFile.setAnimation(R.raw.no_connection)
    }

    private fun getData() {
        args.data.let { data ->
            list.add(
                CoursePreview.VideoCourse(
                    videoPreview = data.previewvideo ?: "",
                    title = data.coursename ?: "No Name",
                    thumbnail = data.thumbnail ?: ""
                )
            )
            list.add(
                CoursePreview.CourseRatingAndOther(
                    rating = data.review?.rateing ?: "4.5",
                    totalHrs = data.totalhrs ?: "00"
                )
            )
            list.add(
                CoursePreview.ArrayClass(
                    title = "For whom this course is for,",
                    targetAudience = data.targetaudience
                )
            )
            list.add(
                CoursePreview.ArrayClass(
                    title = "Requirements for this course,",
                    requirement = data.requirement
                )
            )

            list.add(
                CoursePreview.CoursePrice(
                    currAmt = data.currentprice ?: "00",
                    mrp = data.totalprice ?: "00",
                    title = data.coursename ?: ""
                )
            )
            val review = UserViewOnCourse(
                bywhom = data.review?.bywhom ?: "Anuj",
                rateing = data.review?.rateing ?: "4.5",
                description = data.review?.description
                    ?: "Great to Learn And Improve my Knowledge and Assignment are Best i have learn so much by solving them."
            )
            list.add(CoursePreview.ReviewSection(data = review))
        }
    }

    override fun onPause() {
        super.onPause()
        allPreviewAdaptor = null
        list.clear()
    }

}