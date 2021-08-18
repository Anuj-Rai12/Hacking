package com.example.hackingwork.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hackingwork.R
import com.example.hackingwork.databinding.CloudStorageFragmentBinding
import com.example.hackingwork.utils.*
import com.example.hackingwork.viewmodels.AdminViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CloudStorageFragment : Fragment(R.layout.cloud_storage_fragment) {
    private lateinit var binding: CloudStorageFragmentBinding
    private var extraDialog: ExtraDialog? = null
    private var dialogFlag: Boolean? = null
    private var courseDiff: String? = null
    private val adminViewModel: AdminViewModel by activityViewModels()

    @Inject
    lateinit var customProgress: CustomProgress
    private val courseArrayAdapter: ArrayAdapter<String> by lazy {
        val course = resources.getStringArray(R.array.course_level)
        ArrayAdapter(requireContext(), R.layout.dropdaown, course)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CloudStorageFragmentBinding.bind(view)
        savedInstanceState?.let {
            dialogFlag = it.getBoolean(GetConstStringObj.Create_Course_title)
            courseDiff = it.getString(GetConstStringObj.Create_course)
        }
        if (dialogFlag == true)
            openDialog()
        binding.CourseDifficultLevel.setOnItemClickListener { _, _, position, _ ->
            courseDiff = courseArrayAdapter.getItem(position)
        }
        binding.CreateCourse.setOnClickListener {
            val courseName = binding.setFolderName.text.toString()
            val courseCategory = binding.courseCategory.text.toString()
            val courseRequirement = binding.CourseRequirement.text.toString()
            val courseTarget = binding.CourseTargetAudience.text.toString()
            val courseSalePrice = binding.CourseSalePrice.text.toString()
            val courseDisPrice = binding.CourseDiscountPrice.text.toString()
            if (checkFieldValue(courseCategory) || checkFieldValue(courseDisPrice) || checkFieldValue(
                    courseRequirement
                )
                || checkFieldValue(courseName) || checkFieldValue(courseTarget) || checkFieldValue(
                    courseSalePrice
                )
            ) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.wrong_detail),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

        }
    }

    private fun getValue() {
        lifecycleScope.launch {
            adminViewModel.getCourseContent.collect {
                it?.let { getCourseContent ->
                    FireBaseCourseTitle(
                        coursename = null,
                        courseContent = getCourseContent
                    ).also { Fire ->
                        uploadingCourse(Fire)
                    }
                }
            }
        }
    }

    private fun uploadingCourse(courseContent: FireBaseCourseTitle) {
        adminViewModel.uploadingCourse(courseContent)
    }

    private fun openDialog() {
        extraDialog = ExtraDialog(
            title = GetConstStringObj.Create_Course_title,
            Msg = GetConstStringObj.Create_Course_desc,
            flag = true
        ) {
            if (it)
                getValue()
        }
        extraDialog?.isCancelable = true
        extraDialog?.show(childFragmentManager, "create_course")
        dialogFlag = true
    }

    private fun showLoading(string: String) = customProgress.showLoading(requireActivity(), string)
    private fun hideLoading() = customProgress.hideLoading()
    override fun onPause() {
        super.onPause()
        hideLoading()
        extraDialog?.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dialogFlag?.let {
            outState.putBoolean(GetConstStringObj.Create_Course_title, it)
        }
        courseDiff?.let {
            outState.putString(GetConstStringObj.Create_course, it)
        }
    }
}