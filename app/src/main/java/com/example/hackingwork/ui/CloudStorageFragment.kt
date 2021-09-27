package com.example.hackingwork.ui

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.TAG
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
    private var courseDiff: String? = null
    private var fireBaseCourseTitle: String? = null
    private val adminViewModel: AdminViewModel by activityViewModels()

    @Inject
    lateinit var customProgress: CustomProgress
    private val courseArrayAdapter: ArrayAdapter<String> by lazy {
        val course = resources.getStringArray(R.array.course_level)
        ArrayAdapter(requireContext(), R.layout.dropdaown, course)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CloudStorageFragmentBinding.bind(view)
        setCourseDiff()
        Log.i(TAG, "onViewCreated: Cloud Storage Activated")
        savedInstanceState?.let {
            courseDiff = it.getString(GetConstStringObj.Create_course)
            fireBaseCourseTitle = it.getString(GetConstStringObj.EMAIL)
        }
        fireBaseCourseTitle?.let {
            openDialog()
        }
        binding.CourseDifficultLevel.setOnItemClickListener { _, _, position, _ ->
            courseDiff = courseArrayAdapter.getItem(position)
        }
        binding.UpdateCourse.setOnClickListener {
            val courseName = binding.setFolderName.text.toString()
            if (checkFieldValue(courseName)) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.wrong_detail),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (adminViewModel.getCourseContent.value == null) {
                activity?.msg(" No file to Upload", length = Snackbar.LENGTH_SHORT)
                return@setOnClickListener
            }
            adminViewModel.getCourseContent.asLiveData().observe(viewLifecycleOwner) {
                it?.let { course ->
                    if (course.module != null)
                        showLoading("Uploading New Video...")
                    course.module?.forEach { (moduleKey, moduleValue) ->
                        val flag = course.module.keys.last() == moduleKey
                        moduleValue.video?.forEach { (videoKey, videoValue) ->
                            val vidFlag = moduleValue.video.keys.last() == videoKey
                            updateExitingModuleWithNewVideo(
                                courseName,
                                moduleKey,
                                videoKey,
                                videoValue,
                                flag = flag && vidFlag,
                            ) { bool ->
                                if (bool)
                                    return@updateExitingModuleWithNewVideo
                            }
                        }
                    }
                }
            }
        }
        binding.AddNewModule.setOnClickListener {
            val courseName = binding.setFolderName.text.toString()
            if (checkFieldValue(courseName)) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.wrong_detail),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (adminViewModel.getCourseContent.value == null) {
                activity?.msg(" No file to Upload", length = Snackbar.LENGTH_SHORT)
                return@setOnClickListener
            }
            updateCourseNameForNewModule(courseName)
        }
        binding.CreateCourse.setOnClickListener {
            val courseName = binding.setFolderName.text.toString()
            val courseCategory = binding.courseCategory.text.toString()
            val courseRequirement = binding.CourseRequirement.text.toString()
            val courseTarget = binding.CourseTargetAudience.text.toString()
            val courseSalePrice = binding.CourseSalePrice.text.toString()
            val courseDisPrice = binding.CourseDiscountPrice.text.toString()
            val courseDuration = binding.CourseTotalHrs.text.toString()
            if (checkFieldValue(courseCategory) || checkFieldValue(courseDisPrice) || checkFieldValue(
                    courseRequirement
                )
                || checkFieldValue(courseName) || checkFieldValue(courseTarget) || checkFieldValue(
                    courseSalePrice
                )
                || checkFieldValue(courseDuration) || courseDiff == null
            ) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.wrong_detail),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if ((courseDisPrice.toInt() > (courseSalePrice.toInt()))) {
                Snackbar.make(
                    requireView(),
                    "Discount Price Should be Lower than Sale Price",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (adminViewModel.getCourseContent.value == null) {
                activity?.msg(" No file to Upload", length = Snackbar.LENGTH_SHORT)
                return@setOnClickListener
            }

            val firebase = FireBaseCourseTitle(
                coursename = courseName,
                currentprice = courseDisPrice,
                totalhrs = courseDuration,
                totalprice = courseSalePrice,
                lastdate = getDateTime(),
                requirement = getPathFile(courseRequirement),
                targetaudience = getPathFile(courseTarget),
                category = courseCategory,
                review = null,
                courselevel = courseDiff
            )
            fireBaseCourseTitle = Helper.serializeToJson(firebase)
            openDialog()
        }
    }

    private fun updateExitingModuleWithNewVideo(
        courseName: String,
        moduleKey: String,
        videoKey: String,
        videoValue: Video,
        flag: Boolean,
        error: (Boolean) -> Unit
    ) {
        adminViewModel.addNewVideoInExistingModule(
            courseName = courseName,
            moduleName = moduleKey,
            videoName = videoKey,
            video = videoValue
        ).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    val errorTxt = it.exception?.localizedMessage!!
                    dir(message = "$errorTxt \n\n Try To Correct And Upload Next Time")
                    error(true)
                }
                is MySealed.Loading -> Log.i(TAG, "updateExitingModuleWithNewVideo: Loading ...")
                is MySealed.Success -> {
                    if (flag) {
                        adminViewModel.getCourseContent.value=null
                        hideLoading()
                        dir(title = "Success", message = "All Video Are Uploaded Successfully")
                    }
                }
            }
        }
    }

    private fun updateCourseNameForNewModule(courseName: String) {
        adminViewModel.updateCourseData(courseName).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    Log.i(TAG, "updateCourseNameForNewModule: ${it.exception?.localizedMessage!!}")
                    dir(message = it.exception.localizedMessage!!)
                }
                is MySealed.Loading -> showLoading(it.data as String)
                is MySealed.Success -> {
                    Log.i(TAG, "updateCourseNameForNewModule: Course Updated")
                    updatingNewModule(courseName)
                }
            }
        }
    }

    private fun updatingNewModule(courseName: String) {
        lifecycleScope.launch {
            adminViewModel.getCourseContent.collect {
                it?.let { get ->
                    get.module?.let { map ->
                        map.forEach { (moduleKey, moduleValue) ->
                            uploadVideoModule(
                                moduleKey,
                                moduleValue,
                                courseName,
                                flag = map.keys.last() == moduleKey
                            ) { flag ->
                                if (flag)
                                    return@uploadVideoModule
                            }
                            if (map.values.last() == moduleValue)
                                hideLoading()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setCourseDiff()
    }

    private fun setCourseDiff() {
        val course = resources.getStringArray(R.array.course_level)
        val ad = ArrayAdapter(requireContext(), R.layout.dropdaown, course)
        binding.CourseDifficultLevel.setAdapter(ad)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getValue() {
        lifecycleScope.launch {
            adminViewModel.getCourseContent.collect {
                it?.let { getCourseContent ->
                    val fireBaseCourseTitle =
                        Helper.deserializeFromJson<FireBaseCourseTitle>(fireBaseCourseTitle)
                    FireBaseCourseTitle(
                        coursename = fireBaseCourseTitle?.coursename,
                        courseContent = null,
                        courselevel = fireBaseCourseTitle?.courselevel,
                        category = fireBaseCourseTitle?.category,
                        currentprice = fireBaseCourseTitle?.currentprice,
                        targetaudience = fireBaseCourseTitle?.targetaudience,
                        review = fireBaseCourseTitle?.review,
                        totalprice = fireBaseCourseTitle?.totalprice,
                        lastdate = fireBaseCourseTitle?.lastdate,
                        totalhrs = fireBaseCourseTitle?.totalhrs,
                        requirement = fireBaseCourseTitle?.requirement
                    ).also { Fire ->
                        uploadingCourse(Fire, getCourseContent)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadingCourse(
        courseContent: FireBaseCourseTitle,
        getCourseContent: GetCourseContent
    ) {
        adminViewModel.uploadingCourse(courseContent, getCourseContent)
            .observe(viewLifecycleOwner) {
                when (it) {
                    is MySealed.Error -> {
                        hideLoading()
                        dir(message = it.exception?.localizedMessage ?: "UnWanted Error")
                    }
                    is MySealed.Loading -> {
                        showLoading(it.data as String)
                    }
                    is MySealed.Success -> {
                        getCourseContent.module?.let { map ->
                            map.forEach { (moduleKey, moduleValue) ->
                                val flag = map.keys.last() == moduleKey
                                uploadVideoModule(
                                    moduleKey,
                                    moduleValue,
                                    courseContent.coursename!!,
                                    flag = flag
                                ) { error ->
                                    if (error)
                                        return@uploadVideoModule
                                }
                                if (flag)
                                    hideLoading()
                            }
                        }
                        fireBaseCourseTitle = null
                        if (getCourseContent.module == null) {
                            Toast.makeText(activity, "No File To Upload", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    private fun uploadVideoModule(
        moduleKey: String,
        moduleValue: Module,
        courseName: String,
        flag: Boolean,
        error: (Boolean) -> Unit
    ) {
        adminViewModel.uploadingVideoCourse(moduleKey, moduleValue, courseName)
            .observe(viewLifecycleOwner) {
                when (it) {
                    is MySealed.Error -> {
                        hideLoading()
                        val errorTxt = it.exception?.localizedMessage ?: "UnWanted Error"
                        dir(message = "$errorTxt \n\n Correct This Error And Try Too Upload Next Time.")
                        error(true)
                    }
                    is MySealed.Loading -> Log.i(TAG, "Uploading Course..")
                    is MySealed.Success -> {
                        if (flag) {
                            hideLoading()
                            adminViewModel.getCourseContent.value = null
                            dir(title = "Success", message = "All Module Is Uploaded Successfully")
                        }
                    }
                }
            }
    }

    private fun dir(title: String = "Error", message: String = "") {
        val action =
            CloudStorageFragmentDirections.actionGlobalPasswordDialog2(
                message = message,
                title = title
            )
        findNavController().navigate(action)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun openDialog() {
        extraDialog = ExtraDialog(
            title = GetConstStringObj.Create_Course_title,
            Msg = GetConstStringObj.Create_Course_desc,
            flag = true, function = {
                if (it) {
                    getValue()
                }
            })
        extraDialog?.isCancelable = true
        extraDialog?.show(childFragmentManager, "create_course")
    }

    private fun showLoading(string: String) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        customProgress.showLoading(requireActivity(), string)
    }

    private fun hideLoading() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        customProgress.hideLoading()
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
        extraDialog?.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fireBaseCourseTitle?.let {
            outState.putString(GetConstStringObj.EMAIL, it)
        }
        courseDiff?.let {
            outState.putString(GetConstStringObj.Create_course, it)
        }
    }
}