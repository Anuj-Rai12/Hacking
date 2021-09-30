package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.databinding.MyCourseLayoutBinding
import com.example.hackerstudent.recycle.paid.AllPaidAdaptor
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.CourseViewModel
import com.example.hackerstudent.viewmodels.PrimaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyCourseFragment : Fragment(R.layout.my_course_layout) {
    private lateinit var binding: MyCourseLayoutBinding
    private val viewModel: CourseViewModel by viewModels()
    private val primaryViewModel: PrimaryViewModel by viewModels()
    private val paidCourse: MutableList<PaidCourseSealed> = mutableListOf()
    private val course: MutableList<UploadFireBaseData> = mutableListOf()
    private var allPaidAdaptor: AllPaidAdaptor? = null
    private var flagNoCourse: String? = null

    companion object {
        var userName: String? = null
    }

    @Inject
    lateinit var customProgress: CustomProgress

    @Inject
    lateinit var networkUtils: NetworkUtils

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.changeStatusBarColor()
        showBottomNavBar()
        binding = MyCourseLayoutBinding.bind(view)
        savedInstanceState?.let {
            flagNoCourse = it.getString(GetConstStringObj.UN_WANTED)
        }
        if (flagNoCourse != null)
            noCourse()
        setUpRecycleView()
        if (networkUtils.isConnected() && flagNoCourse == null) {
            internetDevice()
            setRecycleData()
        } else if (!networkUtils.isConnected() && flagNoCourse == null) {
            noInternetDevice()
            activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                if (networkUtils.isConnected()) {
                    setRecycleData()
                }
            })
        }

        binding.arrowImg.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setRecycleData() {
        getUserData {
            paidCourse.add(
                PaidCourseSealed.CourseList(
                    title = "My Course,",
                    uploadFireBaseData = it
                )
            )
            Log.i(TAG, "onViewCreated: Paid Course Data $paidCourse")
            allPaidAdaptor?.submitList(paidCourse)
        }
    }

    private fun setUpRecycleView() {
        binding.courseLayoutRecycle.apply {
            setHasFixedSize(true)
            allPaidAdaptor = AllPaidAdaptor {
                dir(23, title = "${it.fireBaseCourseTitle?.coursename},${it.id}")
            }
            adapter = allPaidAdaptor
        }
    }

    private fun noCourse() {
        binding.courseLottie.show()
        binding.courseLayoutRecycle.hide()
        binding.courseLottie.setAnimation(R.raw.reading_explore)
    }

    private fun getUserData(courseData: (List<UploadFireBaseData>) -> Unit) {
        primaryViewModel.userInfo.observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    noInternetDevice()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    showLoading()
                    internetDevice()
                }
                is MySealed.Success -> {
                    internetDevice()
                    val data = it.data as CreateUserAccount?
                    data?.let { acc ->
                        userName = "${acc.firstname} ${acc.lastname}"
                        paidCourse.add(
                            PaidCourseSealed.User(
                                name = acc.firstname ?: "No Name",
                                layout = R.raw.reading_explore
                            )
                        )
                        acc.courses?.values?.forEach { coursePurchase ->
                            coursePurchase.course?.let { str ->
                                Log.i(TAG, "getUserData: Course Found")
                                val flag = coursePurchase.course == acc.courses.values.last().course
                                getCourseData(str, flag) { list ->
                                    Log.i(TAG, "getUserData: Array list -> $list")
                                    hideLoading()
                                    courseData(list)
                                }
                            }
                        }
                        if (acc.courses?.values.isNullOrEmpty() || acc.courses.isNullOrEmpty()) {
                            hideLoading()
                            flagNoCourse = "Is Empty"
                            noCourse()
                        }
                    }
                }
            }
        }
    }

    private fun getCourseData(
        course: String,
        flag: Boolean,
        getData: (List<UploadFireBaseData>) -> Unit
    ) {
        viewModel.courseID(course).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    noInternetDevice()
                    dir(message = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    internetDevice()
                }
                is MySealed.Success -> {
                    internetDevice()
                    val data = it.data as UploadFireBaseData?
                    data?.let { info ->
                        Log.i(TAG, "getCourseData: Id -> $info")
                        this.course.add(info)
                        if (flag && this.course.isNotEmpty()) {
                            getData(this.course)
                        } else if (flag && this.course.isEmpty()) {
                            flagNoCourse = "Is Empty"
                            noCourse()
                        }
                    }
                }
            }
        }
    }

    private fun dir(choose: Int = 0, title: String = "Error", message: String = "") {
        val action = when (choose) {
            0 -> MyCourseFragmentDirections.actionGlobalPasswordDialog2(title, message)
            else -> {
                val data = getPathFile(title)
                MyCourseFragmentDirections.actionMyCourseFragmentToModuleViewFragment(
                    title = data.first(),
                    id = data.last()
                )
            }
        }
        findNavController().navigate(action)
    }

    private fun internetDevice() {
        binding.courseLayoutRecycle.show()
        binding.courseLottie.hide()
    }

    private fun noInternetDevice() {
        binding.courseLottie.show()
        binding.courseLottie.setAnimation(R.raw.no_connection)
        binding.courseLayoutRecycle.hide()
    }

    fun showLoading() = customProgress.showLoading(requireActivity(), "Loading Paid Course...")
    fun hideLoading() = customProgress.hideLoading()
    override fun onPause() {
        super.onPause()
        paidCourse.clear()
        course.clear()
        allPaidAdaptor = null
        hideLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        flagNoCourse?.let {
            outState.putString(GetConstStringObj.UN_WANTED, it)
        }
    }
}