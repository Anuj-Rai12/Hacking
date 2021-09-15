package com.example.hackerstudent.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hackerstudent.R
import com.example.hackerstudent.TAG
import com.example.hackerstudent.api.Motivation
import com.example.hackerstudent.databinding.HomeScreenFramgmentBinding
import com.example.hackerstudent.recycle.HomeAdaptorView
import com.example.hackerstudent.utils.*
import com.example.hackerstudent.viewmodels.CourseViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeScreenFragment : Fragment(R.layout.home_screen_framgment) {
    private lateinit var binding: HomeScreenFramgmentBinding
    private val courseViewModel: CourseViewModel by viewModels()
    private val courseData: MutableList<CourseSealed> = mutableListOf()

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var customProgress: CustomProgress
    private lateinit var homeAdaptorView: HomeAdaptorView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity?)!!.hide()
        binding = HomeScreenFramgmentBinding.bind(view)
        setUpRecycleView()
        courseData.add(CourseSealed.Image(Id = "Login Creating Image", raw = R.raw.learning))
        if (networkUtils.isConnected()) {
            loadQuote()
        } else {
            binding.noInternet.show()
            binding.noInternet.setAnimation(R.raw.no_connection)
            binding.mainRecycleView.hide()
            activity?.msg(GetConstStringObj.NO_INTERNET, GetConstStringObj.RETRY, {
                if (networkUtils.isConnected()) {
                    Log.i(TAG, "onViewCreated From Retry section : ${networkUtils.isConnected()}")
                    loadQuote()
                }
            })
        }
    }

    private fun getCourseThreeOnly() {
        courseViewModel.courseTodayFirst.observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    dir(msg = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    val course = mutableListOf<UploadFireBaseData>()
                    val data = it.data as MutableList<*>
                    data.forEach { value ->
                        val fireBaseCourseTitle = value as UploadFireBaseData
                        course.add(fireBaseCourseTitle)
                    }
                    courseData.add(
                        CourseSealed.Course(
                            title = getString(R.string.feature_title),
                            fireBaseCourseTitle = course
                        )
                    )
                    homeAdaptorView.submitList(courseData)
                }
            }
        }
    }

    private fun showLoading(string: String) = customProgress.showLoading(requireActivity(), string)
    private fun hideLoading() = customProgress.hideLoading()
    private fun setUpRecycleView() {
        binding.mainRecycleView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            homeAdaptorView = HomeAdaptorView {
                Log.i(TAG, "setUpRecycleView: $it")
                context.msg("go it")
            }
            adapter = homeAdaptorView
        }
    }

    private fun dir(title: String = "Error", msg: String) {
        val action = HomeScreenFragmentDirections.actionGlobalPasswordDialog2(title, msg)
        findNavController().navigate(action)
    }

    private fun loadQuote() {
        binding.noInternet.hide()
        binding.mainRecycleView.show()
        courseViewModel.getTodayQuote.observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    Log.i(TAG, "onViewCreated:")
                    dir(msg = it.exception?.localizedMessage ?: GetConstStringObj.UN_WANTED)
                }
                is MySealed.Loading -> {
                    Log.i(TAG, "onViewCreated:${it.data}")
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    val data = it.data as Motivation
                    courseData.add(CourseSealed.Title(title = data.first().h, motivation = data))
                    getCourseThreeOnly()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }
}