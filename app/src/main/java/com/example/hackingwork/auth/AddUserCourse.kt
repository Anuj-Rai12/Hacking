package com.example.hackingwork.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hackingwork.R
import com.example.hackingwork.databinding.AddUserCourseFragmentBinding
import com.example.hackingwork.utils.*
import com.example.hackingwork.viewmodels.AdminViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddUserCourse : Fragment(R.layout.add_user_course_fragment) {
    private lateinit var binding: AddUserCourseFragmentBinding
    private var getPaymentOption: String? = null
    private val myViewModel: AdminViewModel by viewModels()
    private var userUnpaidAccType: String = "Old Unpaid Account"

    @Inject
    lateinit var customProgress: CustomProgress
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            getPaymentOption = it.getString(GetConstStringObj.Create_Module)
            it.getString(GetConstStringObj.UNPAID)?.let { acc ->
                userUnpaidAccType = acc
            }
        }
        binding = AddUserCourseFragmentBinding.bind(view)
        binding.radioGrp.setOnCheckedChangeListener { _, checkedId ->
            getPaymentOption = when (checkedId) {
                binding.paidOption1.id -> binding.paidOption1.text.toString()
                binding.paidOption2.id -> binding.paidOption2.text.toString()
                else -> null
            }
        }
        binding.radioGrp2.setOnCheckedChangeListener { _, _ ->
            userUnpaidAccType = binding.newUnpaidUser.text.toString()
        }
        binding.AddCourse.setOnClickListener {
            val udi = binding.userCourseId.text.toString()
            val courseName = binding.CourseId.text.toString()
            val amt = binding.courseAmount.text.toString()
            if (checkFieldValue(udi) || checkFieldValue(courseName) || checkFieldValue(amt) || getPaymentOption == null) {
                activity?.msg("Please Enter Correct Info")
                return@setOnClickListener
            }
            if (getPaymentOption == getString(R.string.payment_Option_1)) {
                CourseDetail(
                    course = courseName,
                    data = getDateTime(),
                    purchase = amt
                ).also {
                    val data = UnpaidClass(id = null, courses = mapOf(courseName to it))
                    modifyUnpaid(
                        courseDetail = data,
                        udi = udi,
                        firstTimeAccount = userUnpaidAccType == getString(R.string.newUser_usr)
                    )
                }
            } else {
                CourseDetail(
                    course = courseName,
                    data = getDateTime(),
                    purchase = amt
                ).also {
                    val data = mapOf(courseName to it)
                    modifyPaid(data, udi)
                }

            }
        }
        binding.removedCourse.setOnClickListener {
            val udi = binding.userCourseId.text.toString()
            val courseName = binding.CourseId.text.toString()
            if (checkFieldValue(udi) || getPaymentOption == null || checkFieldValue(courseName)) {
                activity?.msg("Please Enter Correct Info")
                return@setOnClickListener
            }

            if (getPaymentOption == getString(R.string.payment_Option_1)) {
                modifyUnpaid(courseDetail = null, udi = udi, uploadType = false)
            } else {
                CourseDetail().also {
                    val data = mapOf(courseName to it)
                    modifyPaid(data, udi, false)
                }
            }
        }
    }

    private fun modifyPaid(
        data: Map<String, CourseDetail>,
        udi: String,
        uploadType: Boolean = true
    ) {
        myViewModel.modifyPaidUser(data, udi, uploadType).observe(viewLifecycleOwner) {
            when (it) {
                is MySealed.Error -> {
                    hideLoading()
                    dir(
                        title = "Error",
                        msg = it.exception?.localizedMessage ?: "Un Wanted Error"
                    )
                }
                is MySealed.Loading -> {
                    showLoading(it.data as String)
                }
                is MySealed.Success -> {
                    hideLoading()
                    isCheckableRadio()
                    dir(title = "Success", msg = it.data as String)
                }
            }
        }
    }

    private fun isCheckableRadio(flag: Boolean = false) {
        binding.paidOption1.isChecked = flag
        binding.paidOption1.isClickable = true
        binding.paidOption2.isChecked = flag
        binding.paidOption2.isClickable = true
        binding.newUnpaidUser.isChecked = flag
        binding.newUnpaidUser.isClickable = true
    }

    private fun modifyUnpaid(
        courseDetail: UnpaidClass?,
        udi: String,
        uploadType: Boolean = true,
        firstTimeAccount: Boolean = true
    ) {
        myViewModel.modifyUnpaidUser(courseDetail, udi, uploadType, firstTimeAccount)
            .observe(viewLifecycleOwner) {
                when (it) {
                    is MySealed.Error -> {
                        hideLoading()
                        dir(
                            title = "Error",
                            msg = it.exception?.localizedMessage ?: "Un Wanted Error"
                        )
                    }
                    is MySealed.Loading -> {
                        showLoading(it.data as String)
                    }
                    is MySealed.Success -> {
                        hideLoading()
                        isCheckableRadio()
                        dir(title = "Success", msg = it.data as String)
                    }
                }
            }
    }

    private fun hideLoading() = customProgress.hideLoading()
    private fun showLoading(string: String) =
        customProgress.showLoading(context = requireActivity(), string = string)

    private fun dir(title: String, msg: String) {
        val action = AddUserCourseDirections.actionGlobalPasswordDialog2(msg, title)
        findNavController().navigate(action)
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getPaymentOption?.let {
            outState.putString(GetConstStringObj.Create_Module, it)
        }
        outState.putString(GetConstStringObj.UNPAID, userUnpaidAccType)
    }
}